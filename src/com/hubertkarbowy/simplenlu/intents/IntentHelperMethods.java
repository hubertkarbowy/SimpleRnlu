package com.hubertkarbowy.simplenlu.intents;

import java.util.List;

public class IntentHelperMethods {

    // iterates over slots and extracts a value
    static String extractValue (String key, List<String> slots) {
        String val = null;
        for (String slot : slots) {
            if (slot.startsWith("<"+key+":")) {
                val = slot.replaceAll("<"+key+":|>", "");
                System.out.println("From " + slot + " extracting " + key + " = " + val);
            }
        }
        return val;
    }
}
