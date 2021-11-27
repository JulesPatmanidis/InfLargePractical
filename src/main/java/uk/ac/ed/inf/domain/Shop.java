package uk.ac.ed.inf.domain;

/**
 * This class represents Shops from where customers can make an order. Used for JSON parsing.
 */
public class Shop {

    private String name;
    private String location;
    private Item[] menu;

    public String getLocation() {
        return location;
    }

    public Item[] getMenu() {
        return menu;
    }
}
