package uk.ac.ed.inf;

/**
 * This class represents items contained in the menus of each shop. Used for JSON parsing.
 */
public class Item {
    private final String item;
    private final int pence;

    public Item(String item, int pence) {
        this.item = item;
        this.pence = pence;
    }

    public String getItem() {
        return item;
    }

    public int getPence() {
        return pence;
    }
}
