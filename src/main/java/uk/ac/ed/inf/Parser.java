package uk.ac.ed.inf;

import java.util.ArrayList;
import  java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This class is responsible for receiving the raw JSON data from the web server and parsing them into code-readable
 * Java Objects using the Gson library
 */
public class Parser {

    /**
     * Fetches the menu data using the WebFetcher Class and parses the data into an Arraylist of Store objects.
     * @return An Arraylist of type Store populated with the fetched data.
     */
    public static ArrayList<Store> getMenuData() {
        ArrayList<Store> menuData;

        Type listType = new TypeToken<ArrayList<Store>>() {}.getType();
        menuData = new Gson().fromJson(WebFetcher.fetch(WebFetcher.MENUS_PATHNAME), listType);
        return menuData;
    }
}
