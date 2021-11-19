package uk.ac.ed.inf;

import org.junit.Test;

public class webServerTest {

    private static final String WEB_PORT = "9898";

    @Test
    public void testAddressFetch() {
        LongLat longLat = Parser.getLongLatFromW3W("army.monks.grapes");
        System.out.printf("Long, lat: %.6f, %.6f\n", longLat.longitude, longLat.latitude);

    }
}
