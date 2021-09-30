package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * This class serves as an interface between the web server and the rest of the program. I handles requests and fetches
 * data.
 */
public class WebFetcher {

    private static final String BASE_URL = "http://localhost:9898/";
    private static final HttpClient client = HttpClient.newHttpClient();
    public static final String MENUS_PATHNAME = "menus/menus.json";

    /**
     * Fetches the requested json file from the web server and returns its contents as a String for further
     * manipulation.
     *
     * @param target The name of the folder and JSON file on the web server. (e.g.: menus)
     * @return The raw JSON file contents as a String, or an empty String in case of an error.
     */
    public static String fetch(String target) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + target))
                .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Response is not valid, status code: " + response.statusCode());
                return "";
            }
            return response.body();

        } catch (IOException | InterruptedException e) {
            System.err.println("Exception during Http request");
            e.printStackTrace();
            return "";
        }

    }

    /**
     * Helper function that constructs the pathname for the required json file.
     * @param target The name of the target folder.
     * @return A URL that points to the correct json file from the web server
     */
    private static String constructPathName(String target) {
        return BASE_URL + target + "/" + target + ".json";
    }

}
