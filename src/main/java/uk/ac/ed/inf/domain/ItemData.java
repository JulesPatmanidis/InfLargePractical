package uk.ac.ed.inf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *  This class stores item information which exist in the web server and provides methods to access them.
 */
public class ItemData {

    private static final int DELIVERY_CHARGE = 50;
    private static final HashMap<String, Integer> priceMap = new HashMap<>(); /* Maps items to prices */
    private static final HashMap<String, Shop> shopMap = new HashMap<>(); /* Maps items to shops */

    public ItemData(ArrayList<Shop> menuData) {
        loadItemInfo(menuData);
    }

    /**
     * Populates the hashMaps priceMap and shopMap base on the list of Shops parameter.
     * @param menuData a list of Shops
     */
    private void loadItemInfo(ArrayList<Shop> menuData) {
        Objects.requireNonNull(menuData);
        if (menuData.size() == 0) {
            System.err.println("Item info was not parsed correctly");
            System.exit(1);
        }
        for (Shop shop : menuData) {
            for (Item item : shop.getMenu()) {
                priceMap.put(item.getItem(), item.getPence());
                shopMap.put(item.getItem(), shop);
            }
        }
    }

    /**
     * Returns all shops that the drone must pass by given an array of items. The maximum number of shops is 2.
     *
     * @param items the list of items the drone must pick up.
     * @return the list of shops the drone should go to, to collect the orders.
     */
    public List<Shop> findShops(List<String> items) {
        Objects.requireNonNull(items);

        List<Shop> shops =  items.stream()
                .map(shopMap::get)
                .distinct()
                .collect(Collectors.toList());

        if (shops.size() > 2) {
            System.err.println("Only 2 shops are allowed per order!");
            System.exit(1);
        }

        return shops;
    }

    /**
     * Returns the total cost of delivering all the given items by drone, including the standard delivery charge of 50p.
     *
     * @param items A list of items to be delivered.
     * @return The total cost of delivering all the given items, 0 if the list is empty.
     */
    public int calculateDeliveryCost(List<String> items) {
        Objects.requireNonNull(items, "Items list should not be null.");

        int sumWithoutDeliveryFee = items.stream()
                .mapToInt(priceMap::get)
                .sum();

        if (sumWithoutDeliveryFee > 0) {
            return sumWithoutDeliveryFee + DELIVERY_CHARGE;
        } else return 0;
    }
}
