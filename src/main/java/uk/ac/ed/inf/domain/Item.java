package uk.ac.ed.inf.domain;

/**
 * This class represents items contained in the menus of each shop. Used for JSON parsing.
 */
public class Item {
    private String item;
    private int pence;

    public String getItem() {
        return item;
    }

    public int getPence() {
        return pence;
    }
}
