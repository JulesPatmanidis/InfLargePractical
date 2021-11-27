package uk.ac.ed.inf.controller;

import uk.ac.ed.inf.clients.WebServerClient;
import uk.ac.ed.inf.domain.Flightpath;
import uk.ac.ed.inf.domain.ItemData;
import uk.ac.ed.inf.domain.LongLat;
import uk.ac.ed.inf.domain.Order;
import uk.ac.ed.inf.domain.Shop;

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
    private String currentOrderNo;
    private int stepsLeft;
    private final ItemData itemData;
    private final WebServerClient webServerClient;

    private final Pathfinder pathfinder;
    private final List<Flightpath> flightpathList = new ArrayList<>();
    private List<Flightpath> currentOrderFlightpath = new ArrayList<>();


    public DroneController(ItemData itemData, LongLat basePos, int stepsLeft, List<Order> orderList,
                           WebServerClient webServerClient) {
        this.itemData = itemData;
        this.basePos = basePos;
        this.currentPos = basePos; /* Base position and starting position are the same. */
        this.stepsLeft = stepsLeft;
        this.orderList = new LinkedList<>(orderList);
        this.webServerClient = webServerClient;
        this.pathfinder = new Pathfinder(webServerClient.getNoFlyZones());
    }

    public LongLat getCurrentPos() {
        return currentPos;
    }

    public List<Flightpath> getFlightpathList() {
        return flightpathList;
    }

    /**
     * Attempts to deliver the next order in orderList. If the order succeeds, the flightpath of the drone and the
     * delivery are logged and written in the database. Additionally, the flightpaths variable is updated with the
     * complete flightpath of the order. On successful delivery, the order is removed from the orderList.
     *
     * @return True if the order was delivered, False otherwise. (Delivery can fail if the drone runs out of steps)
     */
    public boolean deliverNextOrder() {
        currentOrderFlightpath = new ArrayList<>(); /* Resets the current order flightpath to empty. */
        Order current = orderList.pollFirst();
        if (current == null) {
            System.out.println("Finished orders");
            return false;
        }

        currentOrderNo = current.getOrderNo();
        LongLat customerPos = webServerClient.getLongLatFromW3W(current.getDeliverTo());
        List<String> items = current.getOrderDetails();
        List<Shop> shops = itemData.findShops(items);

        /* This comparator applies only to collections of 2 shops! (which is always the case) */
        Comparator<LongLat> shopComparatorForTwoShops = (o1, o2) -> {
            double dist1 = o1.distanceTo(basePos) + o2.distanceTo(customerPos);
            double dist2 = o1.distanceTo(customerPos) + o2.distanceTo(basePos);

            return Double.compare(dist1, dist2);
        };
        LinkedList<LongLat> targets =  shops.stream()
                .map(shop -> webServerClient.getLongLatFromW3W(shop.getLocation()))
                .sorted(shopComparatorForTwoShops)
                .collect(Collectors.toCollection(LinkedList::new));

        targets.add(customerPos);
        targets.add(basePos);

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
                    makeMove(dest);
                }
            }
            /* If drone did not reach the target, make another step towards it. */
            if (!currentPos.closeTo(currentTarget)) {
                makeMove(currentTarget);
            }
            /* If stop was not at Base, hover to deliver/pickup items. */
            if (targets.size() > 0) {
                deliver();
            }
        }
        if (stepsLeft > 0 && currentPos.closeTo(basePos)) {
            flightpathList.addAll(currentOrderFlightpath);

            return true;
        } else {
            orderList.add(current); /* Order cannot be completed, add order back to the list */
            return false;
        }
    }

    /**
     * Moves the drone from its current position toward the target position and stores the Flightpath describing the
     * move to the currentOrderFlightPath.
     *
     * @param target the target position.
     */
    public void makeMove(LongLat target) {
        List<Integer> possibleAngles;
        int angle;
        LongLat nextPos = new LongLat(0,0);
        boolean found = false;

        if (target.equals(currentPos)) {
            angle = LongLat.HOVER_VALUE;
            nextPos = currentPos.nextPosition(angle);
            found = true;
            System.out.println("Hover");
        } else {
            angle = currentPos.calculateAngle(target);
            /* If the drone cannot move towards angle from currentPos, calculate alternative angles */
            if (!pathfinder.canMoveTowards(currentPos, angle)) {
                possibleAngles = currentPos.calculateAngles(target);
                for (Integer possibleAngle : possibleAngles) {
                    /* If a move can be made towards possibleAngle, test further */
                    if (pathfinder.canMoveTowards(currentPos, possibleAngle)) {
                        LongLat testPos = currentPos.nextPosition(possibleAngle);
                        /* If there is line of sight between the new position and the target, possibleAngle is valid */
                        if (pathfinder.lineOfSight(testPos, target)) {
                            angle = possibleAngle;
                            nextPos = currentPos.nextPosition(angle);
                            found = true;
                            break;
                        }
                    }
                }
            } else {
                nextPos = currentPos.nextPosition(angle);
                found = true;
            }
        }
        if (!found) {
            System.err.println("No angle is valid");
            angle = currentPos.calculateAngle(target);
            nextPos = currentPos.nextPosition(angle);
        }

        completeStep(nextPos, angle);
    }

    /**
     * Makes the drone hover for one move in order to deliver items and stores the appropriate Flightpath object to the
     * currentOrderFlightpath list.
     */
    public void deliver() {
        int angle = LongLat.HOVER_VALUE;
        LongLat nextPos = currentPos.nextPosition(angle);
        completeStep(nextPos, angle);
    }

    /**
     * Logs a new Flightpath object to the currentOrderFlightpath and updates the steps left and the current position.
     *
     * @param nextPos the new position of the drone.
     * @param angle the angle the drone moved towards.
     */
    private void completeStep(LongLat nextPos, int angle) {
        currentOrderFlightpath.add(new Flightpath(currentOrderNo, currentPos, nextPos, angle));
        stepsLeft--;
        currentPos = nextPos;
    }
}
