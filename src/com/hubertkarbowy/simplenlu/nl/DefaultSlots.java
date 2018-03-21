package com.hubertkarbowy.simplenlu.nl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DefaultSlots {

    private static Map<String, BiFunction<String, Map<String, String>, String>> plAliases = new HashMap<>();
    private static Map<String, BiFunction<String, Map<String, String>, String>> enAliases = new HashMap<>();
    private static BiFunction<String, Map<String, String>, String> computeToday = (x,y) -> "18.03.2018";
    private static BiFunction<String, Map<String, String>, String> computeTomorrow= (x,y) -> "19.03.2018";
    private static BiFunction<String, Map<String, String>, String> identity = (x,y) -> y.get(x);

    static {
        plAliases.put("today", identity);
        enAliases.put("tomorrow", computeTomorrow);
        plAliases.put("here", identity);
    }
    static String compute (String slotName, Map<String, String> clientContext, Locale culture) {

        String retVal = null;
        BiFunction<String, Map<String, String>, String> f = null;
        f = plAliases.get(slotName);
        if (f != null) retVal = f.apply(slotName, clientContext);
        return retVal;
    }
}
