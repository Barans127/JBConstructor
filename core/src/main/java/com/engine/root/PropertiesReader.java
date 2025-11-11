package com.engine.root;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.engine.core.ErrorMenu;

import java.util.HashMap;
import java.util.Set;

/** Config file reader. For files with .conf
 * Format -> valueName=value*/
public class PropertiesReader {
    private HashMap<String, String> values;

    public PropertiesReader(){
        values = new HashMap<>();
    }

    /** puts property to list. */
    public boolean addProperty(String key, String value){
        if (key == null || value == null || key.length() == 0){ // negali but null ir neturi but tuscia
            return false;
        }
        if (values.containsKey(key)){
            return false;
        }
        values.put(key, value);
        return true;
    }

    /** loads properties from given file.
     * @param append will append values. if false list will be cleared before adding new values.*/
    public void load(FileHandle file, boolean append){
        if (!file.exists()){
            GdxWrapper.getInstance().setError("PropertiesReader: Cannot locate file: " + file.path(), ErrorMenu.ErrorType.MissingResource);
            return;
        }
        if (!append){
            clear(); // isvalom
        }
        try {
            String content = file.readString();
            String[] lines = content.split("\\r?\\n"); // suskaldom i eilutes.
            for (String line : lines){
                if (line.startsWith("#")) // jei prasideda su situo tai visa eilute komentaras, judam tolyn.
                    continue;
                String lineConent; // ignoruojam komentara.
                if (line.contains("#")) // nukandam komentara.
                    lineConent = line.split("#", 2)[0];
                else
                    lineConent = line;
                if (lineConent.trim().equals("")){
                    continue; // tuscia eilute... judam tolyn
                }
                if (!lineConent.contains("=")) { // kazkas ne to, net neskaitom. metam klaida.
                    throw new GdxRuntimeException("Cannot parse line: " + lineConent);
//                    continue;
                }
                String[] values = lineConent.split("=", 2); // skaldom.
                String key = values[0].trim();
                String value = values[1].trim();
                if (key.length() == 0 || value.length() == 0){ // nedesim tusciu.
                    continue;
                }
                this.values.put(key, value);
            }
        }catch (GdxRuntimeException ex){
            GdxWrapper.getInstance().setError("Error occured while loading properties. " + ex.getMessage(), ErrorMenu.ErrorType.UnknowError);
        }
    }

    /** @return properties key set. */
    public Set<String> getPropertiesKeys(){
        return values.keySet();
    }

    /** @return value. if value by given key not found then null is returned. */
    public String getValue(String key){
        return values.get(key);
    }

    /** @return if properties contains value. */
    public boolean containsValue(String key){
        return values.containsValue(key);
    }

    /** @return if properties contains key. */
    public boolean containsKey(String key){
        return values.containsKey(key);
    }

    /**@return  hashMap used to store properties. */
    public HashMap<String, String> getValuesMap() {
        return values;
    }

    /** clears all properties. */
    public void clear(){
        values.clear();
    }
}
