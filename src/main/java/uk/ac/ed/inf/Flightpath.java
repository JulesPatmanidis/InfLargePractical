package uk.ac.ed.inf;

public class Flightpath {

    private final String orderNo;
    private final LongLat start;
    private final LongLat dest;
    private final int angle;

    public Flightpath(String orderNo, LongLat start, LongLat dest, int angle) {
        this.orderNo = orderNo;
        this.start = start;
        this.dest = dest;
        this.angle = angle;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public LongLat getStart() {
        return start;
    }

    public LongLat getDest() {
        return dest;
    }


    public int getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        return "Flightpath{" +
                "orderNo='" + orderNo + '\'' +
                ", start=" + start +
                ", dest=" + dest +
                ", angle=" + angle +
                '}';
    }
}
