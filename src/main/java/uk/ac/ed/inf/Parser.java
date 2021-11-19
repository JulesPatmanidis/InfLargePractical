package uk.ac.ed.inf;

import java.util.ArrayList;
import  java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;
import uk.ac.ed.inf.Clients.WebServerClient;

/**
 * This class is responsible for receiving the raw JSON data from the web server and parsing them into code-readable
 * Java Objects using the Gson library.
 */
public final class Parser {

    /**
     * Fetches the menu data using the WebFetcher Class and parses the data into an Arraylist of Store objects.
     *
     * @return An Arraylist of type Store populated with the fetched data.
     */
    public static ArrayList<Shop> getMenuData() {
        ArrayList<Shop> menuData;
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        menuData = new Gson().fromJson(WebServerClient.fetchFromServer(WebServerClient.MENUS_PATHNAME), listType);
        return menuData;
    }

    /**
     * Fetches the no-Fly-Zones using the WebFetcher Class and parses the data into an Arraylist of Polygon objects.
     *
     * @return An Arraylist of type Store populated with the fetched data.
     */
    public static ArrayList<Polygon> getNoFlyZones() {
        ArrayList<Polygon> noFlyZones;

        List<Feature> featureList = Objects.requireNonNull(FeatureCollection
                        .fromJson(WebServerClient.fetchFromServer(WebServerClient.NO_FLY_ZONES_PATHNAME))
                        .features());

        noFlyZones = featureList.stream()
                .map(Feature::geometry)
                .filter(geometry -> geometry instanceof Polygon)
                .map(geometry -> (Polygon) geometry)
                .collect(Collectors.toCollection(ArrayList::new));
        return noFlyZones;
    }

    /**
     * Returns the LongLat object represented by the given What3Words address.
     * @param what3WordsText the What3Words address.
     * @return A LongLat object.
     */
    public static LongLat getLongLatFromW3W(String what3WordsText) {
        String wordPath = "/" + what3WordsText.replace('.','/');
        String finalPath = WebServerClient.WORDS_FIRST_PATHNAME + wordPath + WebServerClient.WORDS_LAST_PATHNAME;
        //System.out.println(finalPath);
        Address address =  new Gson().fromJson(WebServerClient.fetchFromServer(finalPath), Address.class);
        return new LongLat(address.coordinates.lng, address.coordinates.lat);
    }

    /**
     * Serialises a list of Flightpath objects to a GSON string. The GSON string has a FeatureCollection containing a
     * single Feature, containing a single LineString with all the points in the given list of Flightpath objects.
     * @param flightpaths
     * @return
     */
    public static String GeoJsonFromFlightpath(List<Flightpath> flightpaths) {
        List<Point> points = new ArrayList<>();
        for (Flightpath path : flightpaths) {
            double lng = path.getStart().longitude;
            double lat = path.getStart().latitude;
            points.add(Point.fromLngLat(lng, lat));
        }
        points.add(Point.fromLngLat(
                flightpaths.get(flightpaths.size() - 1).getDest().longitude,
                flightpaths.get(flightpaths.size() - 1).getDest().latitude)
        );

        return FeatureCollection.fromFeature(Feature.fromGeometry((Geometry) LineString.fromLngLats(points))).toJson();
    }
}
