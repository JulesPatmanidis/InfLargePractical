package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.clients.WebServerClient;
import uk.ac.ed.inf.domain.LongLat;


public class webServerTest {

    private static final String WEB_PORT = "9898";

    @Test
    public void testAddressFetch() {
        WebServerClient webServerClient = new WebServerClient(WEB_PORT);
        LongLat longLat = webServerClient.getLongLatFromW3W("army.monks.grapes");
        System.out.printf("Long, lat: %.6f, %.6f\n", longLat.getLongitude(), longLat.getLatitude());

    }
}
