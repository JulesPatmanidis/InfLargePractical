package uk.ac.ed.inf.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an entry on the database table "orders", combined with the respective entries on
 * "orderDetails".
 */
public class Order {
    private final String orderNo;
    private final java.sql.Date deliveryDate;
    private final String customer;
    private final String deliverTo;
    private final List<String> orderDetails;

    public Order(String orderNo, java.sql.Date deliveryDate, String customer, String deliverTo) {
        this.orderNo = orderNo;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.orderDetails = new ArrayList<>();
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public List<String> getOrderDetails() {
        return orderDetails;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNo='" + orderNo + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", customer='" + customer + '\'' +
                ", deliverTo='" + deliverTo + '\'' +
                ", orderDetailsSize=" + orderDetails.size() +
                '}';
    }
}
