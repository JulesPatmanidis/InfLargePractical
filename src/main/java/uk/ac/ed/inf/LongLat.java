package uk.ac.ed.inf;

import org.checkerframework.checker.units.qual.A;
import uk.ac.ed.inf.Pathfinding.Pathfinder;

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
    public static final double MIN_LATITUDE =  55.942617; //0.003616
    public static final double MAX_LONGITUDE = -3.184319; //3.188408
    public static final double MIN_LONGITUDE = -3.192473;
    public static final double STEP_DISTANCE = 0.00015;
    public static final double CLOSE_DISTANCE = 0.00015; // the distance at which 2 points are considered "close"
    public static final int HOVER_VALUE = -999; // Angle value that represents hovering
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
     *
     * @return True if the point belongs to the confined area, false otherwise.
     */
    public boolean isConfined() {
        return this.longitude < MAX_LONGITUDE && this.longitude > MIN_LONGITUDE
                && this.latitude < MAX_LATITUDE && this.latitude > MIN_LATITUDE;
    }

    /**
     * Returns the Pythagorean distance between two LongLat Objects.
     *
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
     *
     * @param angle The angle of movement of the drone.
     * @return A LongLat object representing the drone's position after a move.
     */
    public LongLat nextPosition(int angle) {
        if (angle == HOVER_VALUE) {
            return new LongLat(this.longitude, this.latitude);
        }
        //angle = (int) (angle + Math.ceil((double) -angle / 360) * 360);
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
     * and is cast to an integer.
     *
     * @param dest the destination.
     * @return the angle as an integer.
     */
    public int calculateAngle(LongLat dest) {
        double angleToTarget = Math.toDegrees(Math.atan2(dest.latitude - this.latitude, dest.longitude - this.longitude));
        return (int) Math.round(angleToTarget / 10) * 10;
    }

    /**
     * Calculates a range of possible intermediate angles between the current LongLat and the destination in the case
     * there is no line-of-sight between them. The calculated angles have  range of exactAngle +-90, where the exact
     * angle is the angle returned from calculateAngle(dest). The order of the angles depends on which unit circle
     * quadrant the angle to the target belongs to.
     * @param dest the destination LongLat.
     * @return a list of possible intermediate angles towards the destination.
     */
    public List<Integer> calculateAngles(LongLat dest) { // Remove prevPos if needed
        List<Integer> angles = new ArrayList<>();
        double angleToTarget = Math.toDegrees(Math.atan2(dest.latitude - this.latitude, dest.longitude - this.longitude));
        angleToTarget = angleToTarget + Math.ceil(-angleToTarget / 360) * 360; // Keep angles between 0 and 360

        int intToTarget = (int) Math.round(angleToTarget / 10) * 10; // Round to nearest 10

        List<Integer> positiveAngles = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // Add angles up to +90 degrees in intervals of 10
                positiveAngles.add(intToTarget + i * 10);
        }
        List<Integer> negativeAngles = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // Add angles up to +90 degrees in intervals of 10
            negativeAngles.add(intToTarget - i * 10);
        }

        if (angleToTarget <= 90 || (angleToTarget <= 270 && angleToTarget > 180)) { // If angle is on Q1 or Q3, first decrease angles
            angles.addAll(negativeAngles);
            angles.addAll(positiveAngles);
        } else if ((angleToTarget > 90 && angleToTarget <= 180) || (angleToTarget > 270 && angleToTarget <= 360)) {
            angles.addAll(positiveAngles);
            angles.addAll(negativeAngles);
        } else {
            System.out.println("Angle is not in any valid quadrant. The system will now exit");
            System.exit(1);
        }
        return angles;
    }

    /**
     * Determines whether a move from the current LongLat with the given angle can be made, given the grid from the
     * pathfinder provided.
     * @param angle the angle of movement.
     * @param pathfinder the pathfinder containing the grid.
     * @return True if a move towards the given angle would be valid, False otherwise.
     */
    public boolean canMoveTowards(int angle, Pathfinder pathfinder) {
        LongLat testPos = this.nextPosition(angle);
        int[] rowCol = Pathfinder.getRowColFromLongLat(testPos);
        boolean walkable = Pathfinder.virtualGrid.get(rowCol[0]).get(rowCol[1]).isWalkable;
        boolean lineOfSight = pathfinder.lineOfSight(this, testPos);
        return  walkable && lineOfSight;
    }

    @Override
    public String toString() {
        return String.format("LongLat{%.6f, %.6f}", longitude, latitude);
    }
}
