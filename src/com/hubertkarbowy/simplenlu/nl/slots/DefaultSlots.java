package com.hubertkarbowy.simplenlu.nl.slots;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DefaultSlots {
    /*
    In all the functions below x is the slot name and y is the ClientContext.

    The values returned by functions that compute default slots should generally be locale-independent
     */

    private static Map<String, BiFunction<String, Map<String, String>, String>> defaultAliases = new HashMap<>();
//    private static Map<String, BiFunction<String, Map<String, String>, String>> enAliases = new HashMap<>();
    private static BiFunction<String, Map<String, String>, String> computeToday = (x,y) -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy"));
    // "today" can be taken from ClientContext. But do I want this?
    private static BiFunction<String, Map<String, String>, String> computeTomorrow = (x,y) -> LocalDate.now().plusDays(1).format((DateTimeFormatter.ofPattern("dd.MM.yyy")));
    private static BiFunction<String, Map<String, String>, String> identity = (x,y) -> y.get(x);

    static {
        defaultAliases.put("today", computeToday);
        defaultAliases.put("tomorrow", computeTomorrow);
        defaultAliases.put("here", identity);
//        enAliases.put("today", computeToday);
//        enAliases.put("tomorrow", computeTomorrow);
//        enAliases.put("here", identity);
    }
    public static String compute (String slotName, Map<String, String> clientContext) {

        String retVal = null;
        BiFunction<String, Map<String, String>, String> f = null;
        f = defaultAliases.get(slotName);
        if (f != null) retVal = f.apply(slotName, clientContext);
        return retVal;
    }
}
