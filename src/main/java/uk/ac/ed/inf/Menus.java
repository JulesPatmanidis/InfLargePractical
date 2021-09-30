package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
public class Menus {

    public static ArrayList<Store> storesData = new ArrayList<>();
    public static final int DELIVERY_CHARGE = 50;

    public String machineName;
    public String serverPort;

    public Menus(String machineName, String serverPort) {
        this.machineName = machineName;
        this.serverPort = serverPort;
        loadStoreInfo();
    }

    /**
     * Uses the Parser class to load all the data concerning the menus of the stores from the web server into the
     * storesData ArrayList.
     */
    private void loadStoreInfo() {
        storesData = Parser.getMenuData();
    }

    /**
     * Returns the total cost of delivering all the given items by drone, including the standard delivery charge of 50p.
     * @param items A (varargs) array of items to be delivered.
     * @return The total cost of delivering all the given items, 0 if the list is empty.
     */
    public int getDeliveryCost(String... items) {
        Objects.requireNonNull(items, "Items list should not be null");

        List<String> itemList = Arrays.asList(items);
        int sumWithoutDeliveryFee = storesData.stream()
                .flatMap(store -> Arrays.stream(store.getMenu()))               // Get all the items in all menus
                .filter(menuEntry -> itemList.contains(menuEntry.getItem()))    // Keep the items included in the input
                .mapToInt(Item::getPence)                                       // Get the cost of each item
                .sum();                                                         // Sum all the costs together

        if (sumWithoutDeliveryFee > 0) {
            return sumWithoutDeliveryFee + DELIVERY_CHARGE;
        } else return 0;
    }
}
