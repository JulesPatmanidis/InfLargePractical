package uk.ac.ed.inf.Clients;

import uk.ac.ed.inf.Delivery;
import uk.ac.ed.inf.Flightpath;
import uk.ac.ed.inf.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for read and write operations on the Derby database.
 */
public class DatabaseClient {

    private Connection conn;
    private Statement statement;

    public DatabaseClient(String port) {
        try {
            String databaseUrl = "jdbc:derby://localhost:" + port + "/derbyDB";
            this.conn = DriverManager.getConnection(databaseUrl);
            this.statement = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("Could not connect to the database, the application will now exit");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Adds a new table to the derby database with all the deliveries completed by the drone.
     *
     * @param content A list of Delivery objects which will be added to the database table.
     */
    public void writeDeliveries(List<Delivery> content) {
        Objects.requireNonNull(content);
        try {
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "DELIVERIES", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }

            statement.execute(
                    "create table deliveries(" +
                            "orderNo char(8), " +
                            "deliveredTo varchar(19), " +
                            "costInPence int)");

            PreparedStatement psDelivery = conn.prepareStatement("insert into deliveries values (?, ?, ?)");

            for (Delivery d : content) {
                psDelivery.setString(1, d.getOrderNo());
                psDelivery.setString(2, d.getDeliveredTo());
                psDelivery.setInt(3, d.getCostInPence());
                psDelivery.execute();
            }
        } catch (SQLException e) {
            System.err.println("Could not write to the database, the application will now exit");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Adds a new table to the derby database with information about the flightpath of the drone. Each step of the drone
     * is described by a flightpath object.
     *
     * @param content A list of Flightpath objects which will be added to the database table.
     */
    public void writeFlightpath(List<Flightpath> content) {
        Objects.requireNonNull(content);
        try {
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }

            statement.execute(
                    "create table flightpath(orderNo char(8)," +
                            "fromLongitude double," +
                            "fromLatitude double," +
                            "angle integer," +
                            "toLongitude double," +
                            "toLatitude double)");

            PreparedStatement psFlightpath = conn.prepareStatement(
                    "insert into flightpath values (?, ?, ?, ?, ?, ?)");

            for (Flightpath f : content) {
                psFlightpath.setString(1, f.getOrderNo());
                psFlightpath.setString(2, Double.toString(f.getStart().longitude));
                psFlightpath.setString(3, Double.toString(f.getStart().latitude));
                psFlightpath.setInt(4, f.getAngle());
                psFlightpath.setString(5, Double.toString(f.getDest().longitude));
                psFlightpath.setString(6, Double.toString(f.getDest().latitude));
                psFlightpath.execute();
            }
        } catch (SQLException e) {
            System.err.println("Could not write to the database, the application will now exit");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Reads all the orders made in the specified date from the database and populates their details.
     *
     * @param date the date when the requested orders were made.
     * @return A list of order objects with their orderDetails filled in.
     */
    public List<Order> readOrders(java.sql.Date date) {
        List<Order> orderList = new ArrayList<>();
        final String ordersQuery = "select * from orders where deliveryDate=(?)";

        try {
            PreparedStatement psOrdersQuery = conn.prepareStatement(ordersQuery);
            psOrdersQuery.setDate(1, date);
            ResultSet rs = psOrdersQuery.executeQuery();
            while (rs.next()) {
                String orderNo = rs.getString("orderNo");
                java.sql.Date orderDate = rs.getDate("deliveryDate");
                String customer = rs.getString("customer");
                String deliverTo = rs.getString("deliverTo");
                orderList.add(new Order(orderNo, orderDate, customer, deliverTo));
            }
        } catch (SQLException e) {
            System.err.println("Could not read orders from the database, the application will now exit");
            e.printStackTrace();
            System.exit(1);
        }

        final String detailsQuery = "select * from orderDetails where orderNo=(?)";
        try {
            PreparedStatement psOrderDetailsQuery = conn.prepareStatement(detailsQuery);
            for(Order order : orderList) {
                psOrderDetailsQuery.setString(1, order.getOrderNo());
                ResultSet rs = psOrderDetailsQuery.executeQuery();
                while (rs.next()) {
                    order.getOrderDetails().add(rs.getString("item"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not read orderDetails from the database, the application will now exit");
            e.printStackTrace();
            System.exit(1);
        }

        if (orderList.isEmpty()) {
            System.err.println("No orders were found for the given date.");
        }
        return orderList;
    }

}
