package uk.ac.ed.inf;
import org.junit.Test;
import uk.ac.ed.inf.clients.DatabaseClient;
import uk.ac.ed.inf.domain.Order;

import static org.junit.Assert.*;
import java.sql.Date;
import java.util.List;

public class DatabaseClientTest {

    private static final String PORT = "9876";

    //Date bounds 01-01-2022 - 31-12-2023
    private static final String TEST_DATE_1 = "2022-01-02";
    private static final String TEST_DATE_2 = "2023-09-10";
    private static final String TEST_INVALID_DATE = "2020-01-01";


    @Test
    public void testReadOrdersA() {
        System.out.println("\nTEST: testReadOrdersA");
        DatabaseClient databaseClient = new DatabaseClient(PORT);
        java.sql.Date date = Date.valueOf(TEST_DATE_1);
        List<Order> orderList = databaseClient.readOrders(date);
        assertFalse(orderList.isEmpty());
        for (Order order : orderList) {
            assertFalse(order.getOrderDetails().isEmpty());
        }
    }

    @Test
    public void testReadOrdersB() {
        System.out.println("\nTEST: testReadOrdersB");
        DatabaseClient databaseClient = new DatabaseClient(PORT);
        java.sql.Date date = Date.valueOf(TEST_DATE_2);
        List<Order> orderList = databaseClient.readOrders(date);
        assertFalse(orderList.isEmpty());
        for (Order order : orderList) {
            assertFalse(order.getOrderDetails().isEmpty());
        }
    }

    @Test
    public void testReadOrdersC() {
        System.out.println("\nTEST: testReadOrdersC");
        DatabaseClient databaseClient = new DatabaseClient(PORT);
        java.sql.Date date = Date.valueOf(TEST_INVALID_DATE);
        List<Order> orderList = databaseClient.readOrders(date);
        assertTrue(orderList.isEmpty());

    }

}
