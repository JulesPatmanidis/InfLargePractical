package uk.ac.ed.inf.clients;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;
import uk.ac.ed.inf.domain.LongLat;
import uk.ac.ed.inf.domain.Address;
import uk.ac.ed.inf.domain.Shop;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class serves as an interface between the web server and the rest of the program. It handles requests and fetches
 * data.
 */
public class WebServerClient {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String FAILED_REQUEST = "";
    private static final int VALID_RESPONSE = 200;
    private static final String MACHINE_NAME = "localhost";
    public static final String MENUS_PATHNAME = "menus/menus.json";
    public static final String NO_FLY_ZONES_PATHNAME = "buildings/no-fly-zones.geojson";
    public static final String WORDS_FIRST_PATHNAME = "words";
    public static final String WORDS_LAST_PATHNAME = "/details.json";

    private final String baseUrl;

    public WebServerClient(String port) {
        this.baseUrl = "http://" + MACHINE_NAME + ":" + port + "/";

    }

    /**
     * Fetches the requested json file from the web server and returns its contents as a String for further
     * manipulation.
     * If connection to the server fails or the response is not 200 (OK), the application exits with error code 1.
     *
     * @param target The name of the folder and JSON file on the web server. (e.g.: menus)
     * @return The raw JSON file contents as a String, or an empty String in case of an error.
     */
    public String fetchFromServer(String target) {
        String jsonResponse = FAILED_REQUEST;

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(baseUrl + target))
                .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() != VALID_RESPONSE) {
                System.err.println("Response is not valid, status code: " + response.statusCode());
            } else {
                jsonResponse = response.body();
            }

        } catch (IOException e) {
            System.err.println("IOException during Http request, check the connection to the server.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted during Http request");
            e.printStackTrace();
        }

        if (jsonResponse.equals(FAILED_REQUEST)) {
            System.err.println("The application will now exit due to a fatal error.");
            System.exit(1);
        }
        return jsonResponse;
    }

    /**
     * Fetches the menu data from the server and parses the data into an Arraylist of Store objects.
     *
     * @return An Arraylist of type Store populated with the fetched data.
     */
    public ArrayList<Shop> getMenuData() {
        ArrayList<Shop> menuData;
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        menuData = new Gson().fromJson(fetchFromServer(WebServerClient.MENUS_PATHNAME), listType);
        return menuData;
    }

    /**
     * Returns the LongLat object represented by the given What3Words address.
     * @param what3WordsText the What3Words address.
     * @return A LongLat object.
     */
    public LongLat getLongLatFromW3W(String what3WordsText) {
        String wordPath = "/" + what3WordsText.replace('.','/');
        String finalPath = WORDS_FIRST_PATHNAME + wordPath + WORDS_LAST_PATHNAME;
        Address address =  new Gson().fromJson(fetchFromServer(finalPath), Address.class);
        return new LongLat(address.coordinates.getLng(), address.coordinates.getLat());
    }

    /**
     * Fetches the no-Fly-Zones from the server and parses the data into an Arraylist of Polygon objects.
     *
     * @return An Arraylist of type Store populated with the fetched data.
     */
    public ArrayList<Polygon> getNoFlyZones() {
        ArrayList<Polygon> noFlyZones;

        List<Feature> featureList = Objects.requireNonNull(FeatureCollection
                .fromJson(fetchFromServer(WebServerClient.NO_FLY_ZONES_PATHNAME))
                .features());

        noFlyZones = featureList.stream()
                .map(Feature::geometry)
                .filter(geometry -> geometry instanceof Polygon)
                .map(geometry -> (Polygon) geometry)
                .collect(Collectors.toCollection(ArrayList::new));
        return noFlyZones;
    }
}
