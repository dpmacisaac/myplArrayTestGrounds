/*
 * File: TypeInfo.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Helper class for holding user-defined (record) type and
 *       function signature information generated from the static
 *       analyzer and used by the (intermediate) code generator.
 */

import java.util.*;

public class ArrInfo {

    // a type has a name and a set of component name-type pairs
    private Map<String,String> arrs = new HashMap<>();

    //Returns the current set of type names being stored.
    public Set<String> getArrs() {
        return arrs.keySet();
    }

    /**
     * Returns the component names for the give type in the order they
     * were added.
     * @param arr the type whose component names are returned
     */
    public String getType(String arr) {
        return arrs.get(arr);
    }

    public void add(String arr, String type) {
        if(!arrs.containsKey(arr)){
            arrs.put(arr, type);
        }
    }

}
