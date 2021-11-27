package uk.ac.ed.inf.utils;

import com.mapbox.geojson.*;
import uk.ac.ed.inf.domain.Flightpath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class includes some utility functions that are not tied to any specific class.
 */
public class Utils {

    /**
     * Serialises a list of Flightpath objects to a GSON string. The GSON string has a FeatureCollection containing a
     * single Feature, containing a single LineString with all the points in the given list of Flightpath objects.
     * @param flightpathList the list of flightpath objects
     * @return a GeoJSON formatted string
     */
    public static String GeoJsonFromFlightpath(List<Flightpath> flightpathList) {
        List<Point> points = new ArrayList<>();
        for (Flightpath path : flightpathList) {
            double lng = path.getStart().getLongitude();
            double lat = path.getStart().getLatitude();
            points.add(Point.fromLngLat(lng, lat));
        }
        points.add(Point.fromLngLat(
                flightpathList.get(flightpathList.size() - 1).getDest().getLongitude(),
                flightpathList.get(flightpathList.size() - 1).getDest().getLatitude())
        );

        return FeatureCollection.fromFeature(Feature.fromGeometry((Geometry) LineString.fromLngLats(points))).toJson();
    }

    /**
     * Creates or overwrites a file with the given name and the given file content.
     * @param fileName the name of the file.
     * @param fileContent the content of the file.
     * @return True if the file was written to successfully, False otherwise.
     */
    public static boolean writeToFile(String fileName, String fileContent) {
        try {
            File file = new File(fileName);
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
            FileWriter writer = new FileWriter(file);
            writer.write(fileContent);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
