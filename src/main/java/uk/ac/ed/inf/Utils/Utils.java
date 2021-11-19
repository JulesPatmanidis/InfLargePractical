package uk.ac.ed.inf.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.stream.Stream;

/**
 * This class includes some utility functions that are not tied to any specific class.
 */
public class Utils {

    /**
     * A contains method for PriorityQueues.
     * @param queue The PriorityQueue to be checked.
     * @param item the item that will be tested/
     * @param <T> the type of the item.
     * @return True if the given queue contains the item, false otherwise.
     */
    public static <T> boolean queueContains(PriorityQueue<T> queue, T item) {
        Stream<T> queueStream$ = queue.stream();
        return queueStream$.anyMatch(x -> x.equals(item));
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
