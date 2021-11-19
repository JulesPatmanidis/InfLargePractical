package uk.ac.ed.inf;

/**
 * This class represents a completed delivery that the drone performed.
 */
public class Delivery {
    private final String orderNo;
    private final String deliveredTo;
    private final int costInPence;

    public Delivery(String orderNo, String deliveredTo, int costInPence) {
        this.orderNo = orderNo;
        this.deliveredTo = deliveredTo;
        this.costInPence = costInPence;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getDeliveredTo() {
        return deliveredTo;
    }

    public int getCostInPence() {
        return costInPence;
    }
}
