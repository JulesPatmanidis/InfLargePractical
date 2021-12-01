package uk.ac.ed.inf.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a Longitude-Latitude coordinate pair and provides all the necessary functions regarding
 * the coordinate system that will is used in this application.
 */

public class LongLat {

    // Min/Max values for latitude/longitude are non-inclusive.
    public static final double MAX_LATITUDE = 55.946233;
    public static final double MIN_LATITUDE =  55.942617;
    public static final double MAX_LONGITUDE = -3.184319;
    public static final double MIN_LONGITUDE = -3.192473;
    public static final double STEP_DISTANCE = 0.00015;
    public static final double CLOSE_DISTANCE = 0.00015; // the distance at which 2 points are considered "close"
    public static final int HOVER_VALUE = -999; // Angle value that represents hovering
    private static final double DISTANCE_ERROR = Math.pow(10, -12); // Decimals less than this value are not considered
    private static final int ALLOWED_ANGLE_MULTIPLE = 10; // Drone movement angle must be a multiple of this value.

    private final double longitude;
    private final double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns true if the coordinates are inside the confined area.
     *
     * @return True if the point belongs to the confined area, false otherwise.
     */
    public boolean isConfined() {
        return this.longitude < MAX_LONGITUDE && this.longitude > MIN_LONGITUDE
                && this.latitude < MAX_LATITUDE && this.latitude > MIN_LATITUDE;
    }

    /**
     * Returns the Pythagorean distance between two LongLat Objects. Distance is determined using the Pythagorean
     * distance formula: dist = Sqrt((x1 - x2)^2 + (y1 - y2)^2)
     *
     * @param dest The destination from where distance is measured (LongLat Object).
     * @return the LongLat object's Pythagorean distance from a given LongLat Object.
     */
    public double distanceTo(LongLat dest) {
        Objects.requireNonNull(dest, "LongLat dest cannot be null.");

        return Math.sqrt(Math.pow(this.longitude - dest.longitude, 2) + Math.pow(this.latitude - dest.latitude, 2));
    }

    /**
     * Returns true if the LongLat object is close to the given one, as defined in the coursework instructions.
     * The "BooleanMethodIsAlwaysInverted" warning is suppressed as the functionality of the method is clearer in this
     * way, despite being inverted with every use.
     *
     * @param target The target LongLat object
     * @return true if the LongLat object is close to the given one
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean closeTo(LongLat target) {
        Objects.requireNonNull(target, "LongLat target cannot be null.");
        return distanceTo(target) - DISTANCE_ERROR <= CLOSE_DISTANCE;
    }

    /**
     * Returns a new LongLat object with the coordinates of the drone after making a move towards the given angle.
     * Special case: The value -999 is reserved for the hover operation which means no movement is performed.
     * If the input is not a multiple of ALLOWED_ANGLE_MULTIPLE, the application exits with error code 1.
     *
     * @param angle The angle of movement of the drone.
     * @return A LongLat object representing the drone's position after a move.
     */
    public LongLat nextPosition(int angle) {
        if (angle == HOVER_VALUE) {
            return new LongLat(this.longitude, this.latitude);
        }

        if ((angle % ALLOWED_ANGLE_MULTIPLE) != 0) {
            System.err.println("Angle parameter must be a multiple of " + ALLOWED_ANGLE_MULTIPLE +", but instead was: " + angle);
            System.exit(1); // Unrecoverable state, program exits gracefully.
        }

        double newLong = this.longitude + (STEP_DISTANCE * Math.cos(Math.toRadians(angle)));
        double newLat = this.latitude + (STEP_DISTANCE * Math.sin(Math.toRadians(angle)));
        return new LongLat(newLong,newLat);
    }

    /**
     * Calculates the angle towards the destination LongLat given. The angle is rounded to the nearest multiple of 10
     * and is cast to an integer. It is also between 0 and 350.
     *
     * @param dest the destination.
     * @return the angle as an integer.
     */
    public int calculateAngle(LongLat dest) {
        double angleToTarget = Math.toDegrees(Math.atan2(dest.latitude - this.latitude, dest.longitude - this.longitude));
        int rounded = (int) Math.round(angleToTarget / 10) * 10;
        if (rounded < 0 ) {
            rounded += 360;
        }

        return rounded;
    }

    /**
     * Calculates a range of intermediate angles between the current LongLat and the destination in the case where
     * there is no line-of-sight between them. The calculated angles have  range of (exactAngle +-90), where the exact
     * angle is the angle returned from calculateAngle(dest). The returned angles essentially create a 180 degree cone
     * pointing towards exactAngle.
     * @param dest the destination LongLat.
     * @return a list of angles towards the destination.
     */
    public List<Integer> calculateAngles(LongLat dest) {
        List<Integer> angles = new ArrayList<>();
        int angleToTarget = this.calculateAngle(dest);

        for (int i = 0; i < 19; i++) { /* Add angles up to +-90 degrees in intervals of 10 */
            int currentN = angleToTarget - i * ALLOWED_ANGLE_MULTIPLE;
            int currentP = angleToTarget + i * ALLOWED_ANGLE_MULTIPLE;
            if (currentN < 0) {
                currentN += 360;
            }
            if (currentP > 360) {
                currentP -= 360;
            }
            angles.add(currentN);
            angles.add(currentP);
        }

        return angles;
    }

    @Override
    public String toString() {
        return String.format("LongLat{%.6f, %.6f}", longitude, latitude);
    }
}
