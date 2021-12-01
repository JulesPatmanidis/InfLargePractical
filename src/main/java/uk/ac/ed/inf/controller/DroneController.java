package uk.ac.ed.inf.controller;

import uk.ac.ed.inf.clients.WebServerClient;
import uk.ac.ed.inf.domain.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handles all interactions with the movement of the drone.
 */
public class DroneController {

    private final LinkedList<Order> orderList;
    private final LongLat basePos;
    private LongLat currentPos;
    private LongLat lastOrderPos;
    private String currentOrderNo;
    private int stepsLeft;
    private int lastOrderSteps;

    private final ItemData itemData;
    private final WebServerClient webServerClient;
    private final Pathfinder pathfinder;

    private final List<Flightpath> flightpathList = new ArrayList<>();
    private List<Flightpath> currentFlightpath = new ArrayList<>();

    public DroneController(ItemData itemData, LongLat basePos, int stepsLeft, List<Order> orderList,
                           WebServerClient webServerClient) {
        this.itemData = itemData;
        this.basePos = basePos;
        this.currentPos = basePos; /* Base position and starting position are the same. */
        this.stepsLeft = stepsLeft;
        this.lastOrderSteps = stepsLeft;
        this.orderList = new LinkedList<>(orderList);
        this.webServerClient = webServerClient;
        this.pathfinder = new Pathfinder(webServerClient.getNoFlyZones());
    }

    public List<Flightpath> getFlightpathList() {
        return flightpathList;
    }


    /**
     * Attempts to deliver all orders in the orderList and returns a list of Delivery objects representing the completed
     * orders.
     * @return the deliveries completed as a list of Delivery objects.
     */
    public List<Delivery> deliverOrders() {
        List<Delivery> completedDeliveries = new ArrayList<>();
        for (Order order : new ArrayList<>(orderList)) {
            boolean delivered = deliverNextOrder();
            int currentCost = itemData.calculateDeliveryCost(order.getOrderDetails());
            if (delivered) {
                completedDeliveries.add(new Delivery(order.getOrderNo(), order.getDeliverTo(), currentCost));
            } else {
                break;
            }
        }
        return completedDeliveries;
    }

    /**
     * Attempts to deliver the next order in orderList. If the order succeeds, the flightpath of the drone and the
     * delivery are logged and written in the database. Additionally, the flightpaths variable is updated with the
     * complete flightpath of the order. On successful delivery, the order is removed from the orderList.
     *
     * @return True if the order was delivered, False otherwise. (Delivery can fail if the drone runs out of steps)
     */
    private boolean deliverNextOrder() {
        currentFlightpath = new ArrayList<>(); /* Resets the current order flightpath to empty. */
        Order current = orderList.pollFirst();
        if (current == null) {
            System.err.println("orderList is empty");
            return false;
        }

        boolean goBackToBase = orderList.size() == 0;

        currentOrderNo = current.getOrderNo();
        LongLat customerPos = webServerClient.getLongLatFromW3W(current.getDeliverTo());
        List<String> items = current.getOrderDetails();
        List<Shop> shops = itemData.findShops(items);

        /* This comparator applies only to collections of 2 shops! (which is always the case) */
        Comparator<LongLat> shopComparatorForTwoShops = (o1, o2) -> {
            double dist1 = o1.distanceTo(currentPos) + o2.distanceTo(customerPos);
            double dist2 = o1.distanceTo(customerPos) + o2.distanceTo(currentPos);

            return Double.compare(dist1, dist2);
        };
        LinkedList<LongLat> targets =  shops.stream()
                .map(shop -> webServerClient.getLongLatFromW3W(shop.getLocation()))
                .sorted(shopComparatorForTwoShops)
                .collect(Collectors.toCollection(LinkedList::new));

        targets.add(customerPos);

        while (!targets.isEmpty()) {
            LongLat currentTarget = targets.pollFirst();
            LinkedList<LongLat> path = new LinkedList<>(pathfinder.findPath(currentPos, currentTarget));
            LongLat dest;

            /* Check if the path given has gaps */
            if (!currentPos.closeTo(path.pollFirst())) {
                System.err.println("Starting position of the drone for the current target disagrees with pathfinder");
                System.exit(1);
            }
            while (!path.isEmpty()) {
                dest = path.pollFirst();
                /* Move towards the destination until the drone is close to it. */
                while (!currentPos.closeTo(dest)) {
                    currentPos = makeMove(currentPos, dest, true);
                    stepsLeft--;
                }
            }
            /* If drone did not reach the target, make another step towards it. This prevents an issue when currentPos
             is close to dest, and dest is closeTo the target, but currentPos is not close to target. */
            if (!currentPos.closeTo(currentTarget)) {
                currentPos = makeMove(currentPos, currentTarget, true);
                stepsLeft--;
            }
            /* Hover to deliver/pickup items. */
            currentPos = deliver(currentPos);
            stepsLeft--;
        }

        if (stepsLeft > 0 && canGoToBase()) {
            flightpathList.addAll(currentFlightpath);
            lastOrderSteps = stepsLeft;
            lastOrderPos = currentPos;
            if (goBackToBase) {
                goBackToBase();
            }

            return true;
        } else {
            orderList.add(current); /* Order cannot be completed, add order back to the list */
            /* Reset drone back to the previous completed order */
            stepsLeft = lastOrderSteps;
            currentPos = lastOrderPos;
            goBackToBase();

            return false;
        }
    }

    /**
     * Moves the drone from its current position back to its base position and logs the flightpath.
     */
    private void goBackToBase() {
        currentFlightpath = new ArrayList<>();
        LinkedList<LongLat> path = new LinkedList<>(pathfinder.findPath(currentPos, basePos));

        LongLat dest;
        while (!path.isEmpty()) {
            dest = path.pollFirst();
            /* Move towards the destination until the drone is close to it. */
            while (!currentPos.closeTo(dest)) {
                currentPos = makeMove(currentPos, dest, true);
                stepsLeft--;
            }
        }
        /* If drone did not reach AT, make another step towards it. */
        if (!currentPos.closeTo(basePos)) {
            currentPos = makeMove(currentPos, basePos, true);
            stepsLeft--;
        }
        flightpathList.addAll(currentFlightpath);
    }

    /**
     * Tests whether the drone has enough moves left to reach its base from its current position.
     *
     * @return True if the drone can return to its base, false otherwise.
     */
    private boolean canGoToBase() {
        LongLat tempPos = currentPos;
        int tempSteps = stepsLeft;

        LinkedList<LongLat> path = new LinkedList<>(pathfinder.findPath(tempPos, basePos));

        LongLat dest;
        while (!path.isEmpty()) {
            dest = path.pollFirst();
            /* Move towards the destination until the drone is close to it. */
            while (!tempPos.closeTo(dest)) {
                tempPos = makeMove(tempPos, dest, false);
                tempSteps--;
            }
        }
        /* If drone did not reach AT, make another step towards it. */
        if (!tempPos.closeTo(basePos)) {
            makeMove(tempPos, basePos, false);
            tempSteps--;
        }
        return tempSteps > 0;
    }

    /**
     * Moves the drone from its current position toward the target position and stores the Flightpath describing the
     * move to the currentOrderFlightPath if the log flag is true.
     *
     * @param origin the drone's current position.
     * @param target the target position.
     * @param log a flag that determines if the move made will be added to the currentFlightpath list.
     *
     * @return a LongLat object describing the new position of the drone.
     */
    private LongLat makeMove(LongLat origin, LongLat target, boolean log) {
        List<Integer> possibleAngles;
        int angle;
        LongLat nextPos = new LongLat(0,0);
        boolean found = false;
        angle = origin.calculateAngle(target);

        /* If the drone cannot move towards angle from currentPos, calculate alternative angles */
        if (!pathfinder.canMoveTowards(origin, angle)) {
            possibleAngles = origin.calculateAngles(target);
            for (Integer possibleAngle : possibleAngles) {
                /* If a move can be made towards possibleAngle, test further */
                if (pathfinder.canMoveTowards(origin, possibleAngle)) {
                    LongLat testPos = origin.nextPosition(possibleAngle);
                    /* If there is line of sight between the new position and the target, possibleAngle is valid */
                    if (pathfinder.lineOfSight(testPos, target)) {
                        angle = possibleAngle;
                        nextPos = origin.nextPosition(angle);
                        found = true;
                        break;
                    }
                }
            }
        } else {
            nextPos = origin.nextPosition(angle);
            found = true;
        }

        if (!found) {
            System.err.println("No valid move was found!");
            angle = origin.calculateAngle(target);
            nextPos = origin.nextPosition(angle);
        }

        if (log) {
            logStep(origin, nextPos, angle);
        }
        return nextPos;
    }

    /**
     * Makes the drone hover for one move in order to deliver items and stores the appropriate Flightpath object to the
     * currentOrderFlightpath list.
     *
     * @param origin the drone's current position.
     * @return a LongLat object describing the new position of the drone.
     */
    private LongLat deliver(LongLat origin) {
        int angle = LongLat.HOVER_VALUE;
        LongLat nextPos = origin.nextPosition(angle);
        logStep(origin, nextPos, angle);
        return nextPos;
    }

    /**
     * Logs a new Flightpath object to the currentOrderFlightpath and updates the steps left and the current position.
     *
     * @param origin the drone's starting position
     * @param nextPos the next position of the drone.
     * @param angle the angle the drone moved towards.
     */
    private void logStep(LongLat origin, LongLat nextPos, int angle) {
        currentFlightpath.add(new Flightpath(currentOrderNo, origin, nextPos, angle));
    }
}
