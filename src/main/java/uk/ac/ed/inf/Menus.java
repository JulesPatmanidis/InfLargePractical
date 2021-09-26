package uk.ac.ed.inf;

public class Menus {

    public String machineName;
    public String serverPort;

    public Menus(String machineName, String serverPort) {
        this.machineName = machineName;
        this.serverPort = serverPort;
    }

    /**
     * Returns the total cost of delivering all the given items by drone, including the standard delivery charge of 50p.
     * @param items A (varargs) array of items to be delivered.
     * @return The total cost of delivering all the given items.
     */
    public int getDeliveryCost(String... items) {
        return 0;
    }
}
