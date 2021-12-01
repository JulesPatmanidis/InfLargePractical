package uk.ac.ed.inf;

import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.clients.WebServerClient;
import uk.ac.ed.inf.domain.*;
import uk.ac.ed.inf.controller.DroneController;
import uk.ac.ed.inf.utils.Utils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class is the main class and the running point of the application.
 *
 */
public class App {

    private static final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private static final int DRONE_STEPS = 1500;

    /**
     * This is the main method and starting point of the application. It is responsible for parsing the user input,
     * initialising all instances required to run the application and calling all methods for the drone to deliver the
     * orders for a given date.
     * @param args command line arguments that represent the date of the delivery, the webserver port and the database
     * port.
     */
    public static void main( String[] args )
    {
        /* Parse command line arguments */
        String dateString = args[2] + "-" + args[1] + "-" + args[0]; //"YYYY-MM-DD";
        String serverPort = args[3];
        String dbPort = args[4];
        String outputFileName = "drone-" + args[0] + "-" + args[1] + "-" + args[2] + ".geojson";

        /* Initialise  */
        DatabaseClient databaseClient = new DatabaseClient(dbPort);
        WebServerClient webServerClient = new WebServerClient(serverPort);
        ItemData itemData = new ItemData(webServerClient.getMenuData());
        List<Order> orders = databaseClient.readOrders(Date.valueOf(dateString));

        /* Sort orders by descending delivery cost */
        orders.sort(Comparator.comparingInt(o -> itemData.calculateDeliveryCost(((Order) o).getOrderDetails())).reversed());

        /* Deliver the orders */
        DroneController droneController =
                new DroneController(itemData, APPLETON_TOWER, DRONE_STEPS, orders, webServerClient);

        int totalMonetaryValue = 0;
        int deliveredMonetaryValue = 0;
        int currentCost;

        for (Order order : orders) {
            currentCost = itemData.calculateDeliveryCost(order.getOrderDetails());
            totalMonetaryValue += currentCost;
        }

        List<Delivery> deliveries = new ArrayList<>(droneController.deliverOrders());
        for (Delivery delivery : deliveries) {
            deliveredMonetaryValue += delivery.getCostInPence();
        }

        System.out.println("Delivered " + deliveries.size() + " out of " + orders.size() + " orders");
        System.out.printf("Percentage monetary value: %.3f%%\n", (deliveredMonetaryValue / totalMonetaryValue) * 100d);

        /* Write to the database */
        databaseClient.writeDeliveries(deliveries);
        databaseClient.writeFlightpath(droneController.getFlightpathList());

        /* Write to file */
        String output = Utils.GeoJsonFromFlightpath(droneController.getFlightpathList());

        if (Utils.writeToFile(outputFileName, output)) {
            System.out.println("Output file written successfully.");
        } else {
            System.err.println("Could not write to file.");
        }
    }
}
