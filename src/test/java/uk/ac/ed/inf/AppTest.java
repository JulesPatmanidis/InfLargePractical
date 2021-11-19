package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.Clients.DatabaseClient;

import java.time.LocalDate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AppTest {

    private static final String VERSION = "1.0.5";
    private static final String RELEASE_DATE = "September 28, 2021";
    private static final String TEST_DATE_1 = "2022-01-02";
    private static final String TEST_DATE_2 = "2023-09-10";
    private static final String TEST_DATE_3 = "2022-09-15";
    private static final String TEST_DATE_4 = "2023-09-10";
    private static final String MIN_DATE = "2022-01-01";
    private static final String MAX_DATE = "2023-12-31";
    private static final int SAMPLE_COUNT = 30;


    LongLat AT = new LongLat(-3.186874, 55.944494); // Appleton Tower

    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);
    private final LongLat businessSchool = new LongLat(-3.1873,55.9430);
    private final LongLat greyfriarsKirkyard = new LongLat(-3.1928,55.9469);

    @Test
    public void testIsConfinedTrueA(){
        assertTrue(appletonTower.isConfined());
    }

    @Test
    public void testIsConfinedTrueB(){
        assertTrue(businessSchool.isConfined());
    }

    @Test
    public void testIsConfinedFalse(){
        assertFalse(greyfriarsKirkyard.isConfined());
    }

    private boolean approxEq(double d1, double d2) {
        return Math.abs(d1 - d2) < 1e-12;
    }

    @Test
    public void testDistanceTo(){
        double calculatedDistance = 0.0015535481968716011;
        System.out.printf("calculated: %.15f\n", appletonTower.distanceTo(businessSchool));
        assertTrue(approxEq(appletonTower.distanceTo(businessSchool), calculatedDistance));
    }

    @Test
    public void testCloseToTrue(){
        LongLat alsoAppletonTower = new LongLat(-3.186767933982822, 55.94460006601717);
        assertTrue(appletonTower.closeTo(alsoAppletonTower));
    }


    @Test
    public void testCloseToFalse(){
        assertFalse(appletonTower.closeTo(businessSchool));
    }


    private boolean approxEq(LongLat l1, LongLat l2) {
        return approxEq(l1.longitude, l2.longitude) &&
                approxEq(l1.latitude, l2.latitude);
    }

    @Test
    public void testAngle0(){
        LongLat nextPosition = appletonTower.nextPosition(0);
        LongLat calculatedPosition = new LongLat(-3.186724, 55.944494);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle20(){
        LongLat nextPosition = appletonTower.nextPosition(20);
        LongLat calculatedPosition = new LongLat(-3.186733046106882, 55.9445453030215);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle50(){
        LongLat nextPosition = appletonTower.nextPosition(50);
        LongLat calculatedPosition = new LongLat(-3.186777581858547, 55.94460890666647);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle90(){
        LongLat nextPosition = appletonTower.nextPosition(90);
        LongLat calculatedPosition = new LongLat(-3.186874, 55.944644);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle140(){
        LongLat nextPosition = appletonTower.nextPosition(140);
        LongLat calculatedPosition = new LongLat(-3.1869889066664676, 55.94459041814145);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle190(){
        LongLat nextPosition = appletonTower.nextPosition(190);
        LongLat calculatedPosition = new LongLat(-3.1870217211629517, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle260(){
        LongLat nextPosition = appletonTower.nextPosition(260);
        LongLat calculatedPosition = new LongLat(-3.18690004722665, 55.944346278837045);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle300(){
        LongLat nextPosition = appletonTower.nextPosition(300);
        LongLat calculatedPosition = new LongLat(-3.186799, 55.94436409618943);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle350(){
        LongLat nextPosition = appletonTower.nextPosition(350);
        LongLat calculatedPosition = new LongLat(-3.1867262788370483, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle999(){
        // The special junk value -999 means "hover and do not change position"
        LongLat nextPosition = appletonTower.nextPosition(-999);
        assertTrue(approxEq(nextPosition, appletonTower));
    }

    @Test
    public void testMenusOne() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 50, totalCost);
    }

    @Test
    public void testMenusTwo() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 50, totalCost);
    }

    @Test
    public void testMenusThree() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourA() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte",
                "Dirty matcha latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourB() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Flaming tiger latte",
                "Dirty matcha latte",
                "Strawberry matcha latte",
                "Fresh taro latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(4 * 460 + 50, totalCost);
    }



    @Test
    public void testDroneController() {
        Menus menus = new Menus("localhost", "9898");
        DatabaseClient databaseClient = new DatabaseClient("9876");
        List<Order> orders = databaseClient.readOrders(Date.valueOf(TEST_DATE_2));
        DroneController droneController = new DroneController(menus, AT, 1500, orders);

        int totalMonetaryValue = 0;
        int deliveredMonetaryValue = 0;

        int currentCost;

        for (Order order : orders) {
            boolean delivered = droneController.deliverNextOrder();
            currentCost = menus.getDeliveryCost(order.getOrderDetails().toArray(new String[0]));
            totalMonetaryValue += currentCost;
            if (delivered) {
                deliveredMonetaryValue += currentCost;
            }
        }
        System.out.println(totalMonetaryValue);
        System.out.println(deliveredMonetaryValue);
        System.out.printf("Percentage monetary value: %.1f%%\n", ((double) deliveredMonetaryValue / totalMonetaryValue) * 100d);



        assertTrue(droneController.currentPos.closeTo(AT));
        assertFalse(droneController.flightpaths.isEmpty());
        //assertTrue(droneController.stepsLeft >= 0);

        try {
            File file = new File("TestOutput.geojson");
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter writer = new FileWriter(file);
            writer.write(Parser.GeoJsonFromFlightpath(droneController.flightpaths));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void allDatesRunTest() {
        double sAvgMonetaryValue = 0;
        Menus menus = new Menus("localhost", "9898");
        DatabaseClient databaseClient = new DatabaseClient("9876");
        List<String> dates = new ArrayList<>();
        LocalDate localeDate;
        Date firstDate = Date.valueOf("2022-01-01");
        String currentDate = firstDate.toString();
        while (!currentDate.equals("2023-12-31")) {
            dates.add(currentDate);

            localeDate = LocalDate.parse(currentDate);
            currentDate = localeDate.plusDays(1).toString();
        }
        int count = 0;
        for (String date : dates) {
            sAvgMonetaryValue += runForDate(date, menus, databaseClient);
            count++;
        }
        sAvgMonetaryValue = sAvgMonetaryValue / count;
        System.out.printf("Sampled Average Percentage monetary value of the total of %d days: %.1f%%\n", count, sAvgMonetaryValue);
    }

    @Test
    public void getSampledAverageMonetaryValue() {
        System.out.println("Calculating Sampled Average Percentage Monetary Value for " + SAMPLE_COUNT + " random dates.");
        double sAvgMonetaryValue = 0;

        Menus menus = new Menus("localhost", "9898");
        DatabaseClient databaseClient = new DatabaseClient("9876");
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int year = (int) Math.round(Math.random()) + 2022;
            int month = (int) Math.floor(Math.random() * 12) + 1;
            int day = (int) Math.floor(Math.random() * 28) + 1;
            dates.add(String.format("%d-%02d-%02d", year, month, day));
        }

        for (String date : dates) {
            sAvgMonetaryValue += runForDate(date, menus, databaseClient);
        }

        sAvgMonetaryValue = sAvgMonetaryValue / SAMPLE_COUNT;
        System.out.printf("Sampled Average Percentage monetary value: %.1f%%\n", sAvgMonetaryValue);
    }

    public double runForDate(String date, Menus menus, DatabaseClient databaseClient) {
        List<Order> orders = databaseClient.readOrders(Date.valueOf(date));
        DroneController droneController = new DroneController(menus, AT, 1500, orders);

        int totalMonetaryValue = 0;
        int deliveredMonetaryValue = 0;

        int currentCost;

        for (Order order : orders) {
            boolean delivered = droneController.deliverNextOrder();
            currentCost = menus.getDeliveryCost(order.getOrderDetails().toArray(new String[0]));
            totalMonetaryValue += currentCost;
            if (delivered) {
                deliveredMonetaryValue += currentCost;
            }
        }
        return  ((double) deliveredMonetaryValue / totalMonetaryValue) * 100d;
    }


}