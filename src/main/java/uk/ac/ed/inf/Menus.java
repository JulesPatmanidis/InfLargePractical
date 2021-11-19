package uk.ac.ed.inf;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  This class holds the shop information held in the web server.
 */
public class Menus {

    public static final int DELIVERY_CHARGE = 50;
    public static ArrayList<Shop> shopsData = new ArrayList<>();
    public static HashMap<String, Integer> priceMap = new HashMap<>();


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
        shopsData = Parser.getMenuData();
        for (Shop shop : shopsData) {
            for (Item item : shop.getMenu()) {
                priceMap.put(item.getItem(), item.getPence());
            }
        }
        if (shopsData == null) {
            System.exit(1);
        }
    }

    /**
     * Returns all shops that the drone must pass by given an array of items.
     *
     * @param items the array of items the drone must pick up.
     * @return the list of shops the drone should go to, to collect the orders.
     */
    public List<Shop> getShops(String... items) {
        Objects.requireNonNull(items);
        List<String> itemList = Arrays.asList(items);

        return shopsData.stream()
                .filter(shop -> Arrays.stream(shop.getMenu())
                        .map(Item::getItem)
                        .anyMatch(itemList::contains))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total cost of delivering all the given items by drone, including the standard delivery charge of 50p.
     *
     * @param items A (varargs) array of items to be delivered.
     * @return The total cost of delivering all the given items, 0 if the list is empty.
     */
    public int getDeliveryCost(String... items) {
        Objects.requireNonNull(items, "Items list should not be null.");
        List<String> itemList = Arrays.asList(items);
        int sumWithoutDeliveryFee = itemList.stream()
                .mapToInt(name -> priceMap.get(name))
                .sum();

        if (sumWithoutDeliveryFee > 0) {
            return sumWithoutDeliveryFee + DELIVERY_CHARGE;
        } else return 0;
    }
}
