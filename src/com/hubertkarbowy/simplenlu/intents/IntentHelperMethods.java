package com.hubertkarbowy.simplenlu.intents;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    static String capitalizeEachWord (String seq) {
        return String.join(" ", Arrays.asList(seq.split(" ")).stream().map(x -> x.substring(0,1).toUpperCase() + x.substring(1)).collect(Collectors.toList()));
    }
}
