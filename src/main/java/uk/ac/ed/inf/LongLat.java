package uk.ac.ed.inf;

import java.util.Objects;

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
    private static final double STEP_DISTANCE = 0.00015;
    private static final double CLOSE_DISTANCE = 0.00015; // the distance at which 2 points are considered "close"
    private static final int HOVER_VALUE = -999; // Angle value that represents hovering
    private static final double DISTANCE_ERROR = Math.pow(10, -12); // Decimals less than this value are not considered
    private static final int ALLOWED_ANGLE_MULTIPLE = 10; // Drone movement angle must be a multiple of this value.

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
        Objects.requireNonNull(dest, "LongLat dest cannot be null.");
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
        Objects.requireNonNull(target, "LongLat target cannot be null.");
        return distanceTo(target) - DISTANCE_ERROR <= CLOSE_DISTANCE;
    }

    /**
     * Returns a new LongLat object with the coordinates of the drone after making a move towards the given angle.
     * Special case: The value -999 is reserved for the hover operation which means no movement is performed.
     * If the input is not a multiple of ALLOWED_ANGLE_MULTIPLE, the application exits with error code 1.
     * @param angle The angle of movement of the drone.
     * @return A LongLat object representing the drone's position after a move.
     */
    public LongLat nextPosition(int angle) {
        if (angle == HOVER_VALUE) {
            return new LongLat(this.longitude, this.latitude);
        }

        if ((angle % ALLOWED_ANGLE_MULTIPLE) != 0) {
            System.err.println("Angle parameter must be a multiple of 10, but instead was: " + angle);
            System.exit(1); // Unrecoverable state, program exits gracefully.
        }

        double newLong = this.longitude + (STEP_DISTANCE * Math.cos(Math.toRadians(angle)));
        double newLat = this.latitude + (STEP_DISTANCE * Math.sin(Math.toRadians(angle)));
        return new LongLat(newLong,newLat);
    }
}
