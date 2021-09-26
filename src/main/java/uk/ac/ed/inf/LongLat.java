package uk.ac.ed.inf;

/**
 * This class represents a Longitude-Latitude coordinate pair and provides all the necessary functions regarding
 * the coordinate system that will is used in this application.
 */

public class LongLat {

    public double longitude;
    public double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Returns true if the coordinates are inside a confined area.
     * @return True if the point belongs to a confined area, false otherwise.
     */
    public boolean isConfined() {
        // Return true if point is in confined space
        return true;
    }

    /**
     * Returns the distance between two LongLat Objects.
     * @param dest The destination from where distance is measured (LongLat Object).
     * @return the LongLat object's distance from a given LongLat Object.
     */
    public double distanceTo(LongLat dest) {
        return 0d;
    }

    /**
     * Returns true if the LongLat object is close to the given one, as defined in the coursework instructions.
     * @param target The target LongLat object
     * @return true if the LongLat object is close to the given one
     */
    public boolean closeTo(LongLat target) {
        return true;
    }

    /**
     * Returns a new LongLat object with the coordinates of the drone after making a move towards the given angle.
     * @param angle The angle of movement of the drone.
     * @return A LongLat object representing the drone's position after a move.
     */
    public LongLat nextPosition(int angle) {
        return new LongLat(0,0);
    }


}
