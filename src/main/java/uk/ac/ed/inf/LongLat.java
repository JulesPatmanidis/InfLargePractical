package uk.ac.ed.inf;

/**
 * This class represents a Longitude-Latitude coordinate pair and provides all the necessary functions regarding
 * the coordinate system that will is used in this application.
 */

public class LongLat {

    // Min/Max values for latitude/longitude are non-inclusive.
    private static final double MAX_LATITUDE = 55.946233;
    private static final double MIN_LATITUDE =  55.942617;
    private static final double MAX_LONGITUDE = -3.184319;
    private static final double MIN_LONGITUDE = -3.192473;
    private static final double CLOSE_DISTANCE = 0.00015; // the distance at which 2 points are considered "close"
    private static final double STEP_DISTANCE = 0.00015;
    private static final double DISTANCE_ERROR = Math.pow(10, -12); // decimals less than this value are not considered

    public double longitude;
    public double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Returns true if the coordinates are inside the confined area.
     * @return True if the point belongs to the confined area, false otherwise.
     */
    public boolean isConfined() {
        return this.longitude < MAX_LONGITUDE && this.longitude > MIN_LONGITUDE
                && this.latitude < MAX_LATITUDE && this.latitude > MIN_LATITUDE;
    }

    /**
     * Returns the Pythagorean distance between two LongLat Objects.
     * @param dest The destination from where distance is measured (LongLat Object).
     * @return the LongLat object's Pythagorean distance from a given LongLat Object.
     */
    public double distanceTo(LongLat dest) {
        // Pythagorean distance formula: Sqrt((x1 - x2)^2 + (y1 - y2)^2)
        return Math.sqrt(Math.pow(this.longitude - dest.longitude, 2) + Math.pow(this.latitude - dest.latitude, 2));
    }

    /**
     * Returns true if the LongLat object is close to the given one, as defined in the coursework instructions.
     * 
     * @param target The target LongLat object
     * @return true if the LongLat object is close to the given one
     */
    public boolean closeTo(LongLat target) {
        return distanceTo(target) - DISTANCE_ERROR <= CLOSE_DISTANCE;
    }

    /**
     * Returns a new LongLat object with the coordinates of the drone after making a move towards the given angle.
     * Special case: The value -999 is reserved for the hover operation which means no movement is performed.
     * @param angle The angle of movement of the drone.
     * @return A LongLat object representing the drone's position after a move.
     */
    public LongLat nextPosition(int angle) {
        if (angle == -999) {
            return new LongLat(this.longitude, this.latitude);
        }

        double newLong = this.longitude + (STEP_DISTANCE * Math.cos(Math.toRadians(angle)));
        double newLat = this.latitude + (STEP_DISTANCE * Math.sin(Math.toRadians(angle)));
        return new LongLat(newLong,newLat);
    }
}
