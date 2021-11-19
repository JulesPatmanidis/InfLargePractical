package uk.ac.ed.inf.Clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * This class serves as an interface between the web server and the rest of the program. It handles requests and fetches
 * data.
 */
public final class WebServerClient {

    private static final String BASE_URL = "http://localhost:9898/";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String FAILED_REQUEST = "";
    public static final String MENUS_PATHNAME = "menus/menus.json";
    public static final String NO_FLY_ZONES_PATHNAME = "buildings/no-fly-zones.geojson";
    public static final String WORDS_FIRST_PATHNAME = "words";
    public static final String WORDS_LAST_PATHNAME = "/details.json";

    /**
     * Fetches the requested json file from the web server and returns its contents as a String for further
     * manipulation.
     * If connection to the server fails or the response is not 200 (OK), the application exits with error code 1.
     *
     * @param target The name of the folder and JSON file on the web server. (e.g.: menus)
     * @return The raw JSON file contents as a String, or an empty String in case of an error.
     */
    public static String fetchFromServer(String target) {
        String jsonResponse = FAILED_REQUEST;

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(BASE_URL + target))
                .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Response is not valid, status code: " + response.statusCode());
            } else {
                jsonResponse = response.body();
            }

        } catch (IOException e) {
            System.err.println("IOException during Http request, check the connection to the server.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted during Http request, ");
            e.printStackTrace();
        }

        if (jsonResponse.equals(FAILED_REQUEST)) {
            System.err.println("The application will now exit due to a fatal error.");
            System.exit(1);
        }
        return jsonResponse;
    }
}
