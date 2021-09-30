package uk.ac.ed.inf;

/**
 * This class is the main class and the running point of the application.
 *
 */
public class App {

    public static void main( String[] args )
    {
        Menus menus = new Menus("localhost", "9898");
        System.out.println(menus.getDeliveryCost("Hummus and salad Italian roll"));
    }
}
