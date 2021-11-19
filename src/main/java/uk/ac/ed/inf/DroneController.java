package uk.ac.ed.inf;

import uk.ac.ed.inf.Pathfinding.Pathfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handles all interactions with the movement of the drone.
 */
public class DroneController {

    public LinkedList<Order> orderList;
    public LongLat basePos;
    public LongLat currentPos;
    public String currentOrderNo;
    public int stepsLeft;
    //public int stepsExecuted = 0;
    public Menus menus;
    public static final Pathfinder pathfinder = new Pathfinder();
    private List<Flightpath> currentOrderFlightpath = new ArrayList<>();
    public List<Flightpath> flightpaths = new ArrayList<>();

    public DroneController(Menus menus, LongLat basePos, int stepsLeft, List<Order> orderList) {
        this.menus = menus;
        this.basePos = basePos;
        this.currentPos = basePos; // Base position and starting position are the same.
        this.stepsLeft = stepsLeft;
        this.orderList = new LinkedList<>(orderList);
    }

    /**
     * Attempts to deliver the next order in orderList. If the order succeeds, the flightpath of the drone and the
     * delivery are logged and written in the database. Additionally, the flightpaths variable is updated with the
     * complete flightpath of the order.
     *
     * @return True if the order was delivered, False otherwise. (Delivery can fail if the drone runs out of steps)
     */
    public boolean deliverNextOrder() {
        currentOrderFlightpath = new ArrayList<>(); // Resets the current order flightpath to empty.
        Order current = orderList.pollFirst();
        if (current == null) {
            System.out.println("Finished orders");
            return false;
        }

        currentOrderNo = current.getOrderNo();
        LongLat customerPos = Parser.getLongLatFromW3W(current.getDeliverTo()); // maybe have a hashmap for caching

        List<String> items = current.getOrderDetails();
        LinkedList<Shop> shops = new LinkedList<>(menus.getShops(items.toArray(new String[0])));
        // Edit shop order?

        Comparator<LongLat> shopComparatorForTwoShops = (o1, o2) -> {
            double dist1 = o1.distanceTo(basePos) + o2.distanceTo(customerPos);
            double dist2 = o1.distanceTo(customerPos) + o2.distanceTo(basePos);

            return Double.compare(dist1, dist2);
        };

        LinkedList<LongLat> targets =  shops.stream()
                .map(shop -> Parser.getLongLatFromW3W(shop.getLocation()))
                //.sorted(shopComparatorForTwoShops)
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
            if (!currentPos.closeTo(currentTarget)) { //
                makeMove(currentTarget);
            }

            /* If stop was not at Base, hover to deliver/pickup items. */
            if (targets.size() > 0) {
                deliver();
            }
        }

        if (!currentPos.closeTo(basePos)) {
            System.out.println(currentPos + " not close to " + basePos);
        }

        if (stepsLeft > 0 && currentPos.closeTo(basePos)) {
            flightpaths.addAll(currentOrderFlightpath);
            return true;
        } else {
            orderList.add(current);
            return false;
        }
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
            if (!currentPos.canMoveTowards(angle, pathfinder)) {
                possibleAngles = currentPos.calculateAngles(target);
                for (Integer possibleAngle : possibleAngles) {
                    if (currentPos.canMoveTowards(possibleAngle, pathfinder)) {
                        LongLat testPos = currentPos.nextPosition(possibleAngle);
                        if (testPos.canMoveTowards(testPos.calculateAngle(target), pathfinder)) {
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
        if (!pathfinder.lineOfSight(currentPos, nextPos)) {
            System.err.println("No line of sight!");
        }
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
