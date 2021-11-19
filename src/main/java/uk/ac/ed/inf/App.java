package uk.ac.ed.inf;

import uk.ac.ed.inf.Clients.DatabaseClient;
import uk.ac.ed.inf.Utils.Utils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main class and the running point of the application.
 *
 */
public class App {

    private static final String MACHINE_NAME = "localHost";
    private static final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private static final int DRONE_STEPS = 1500;

    /**
     * This is the main method and starting point of the application. It is responsible for parsing the user input,
     * initialising all instances required to run the application and calling all methods for the drone to deliver the
     * orders for a given date.
     * @param args command line arguments that represent the date of the delivery, the webserver port and the database
     *             port
     */
    public static void main( String[] args )
    {
        String dateString = args[2] + "-" + args[1] + "-" + args[0]; //"2022-12-12";
        String serverPort = args[3];
        String dbPort = args[4];
        String outputFileName = "drone-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson";

        Menus menus = new Menus(MACHINE_NAME, serverPort);
        DatabaseClient databaseClient = new DatabaseClient(dbPort);
        List<Order> orders = databaseClient.readOrders(Date.valueOf(dateString));
        DroneController droneController = new DroneController(menus, APPLETON_TOWER, DRONE_STEPS, orders);

        List<Delivery> deliveries = new ArrayList<>();
        int totalMonetaryValue = 0;
        int deliveredMonetaryValue = 0;

        int currentCost;

        for (Order order : orders) {
            boolean delivered = droneController.deliverNextOrder();
            currentCost = menus.getDeliveryCost(order.getOrderDetails().toArray(new String[0]));
            totalMonetaryValue += currentCost;
            if (delivered) {
                deliveredMonetaryValue += currentCost;
                deliveries.add(new Delivery(order.getOrderNo(), order.getDeliverTo(), currentCost));
            }
        }

        System.out.printf("Percentage monetary value: %.3f%%\n", (deliveredMonetaryValue / totalMonetaryValue) * 100d);

        databaseClient.writeDeliveries(deliveries);
        databaseClient.writeFlightpath(droneController.flightpaths);
        String output = Parser.GeoJsonFromFlightpath(droneController.flightpaths);
//        System.out.printf("pos: (%.6f, %.6f) | steps left: %d\n",
//                droneController.currentPos.longitude, droneController.currentPos.latitude, droneController.stepsLeft);

        if (Utils.writeToFile(outputFileName, output)) {
            System.out.println("Output file written successfully.");
        } else {
            System.err.println("Could not write to file.");
        }
    }
}
