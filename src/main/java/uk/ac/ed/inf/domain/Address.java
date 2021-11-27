package uk.ac.ed.inf.domain;

/**
 * This class represents addresses stored in the web server. Used only for JSON parsing.
 */
public class Address {
    public transient String country;
    public transient String square;
    public transient String nearestPlace;
    public LngLat coordinates;
    public String words;
    public transient String language;
    public transient String map;

    public static class LngLat {
        private double lng;
        private double lat;

        public double getLng() {
            return lng;
        }

        public double getLat() {
            return lat;
        }
    }
}
