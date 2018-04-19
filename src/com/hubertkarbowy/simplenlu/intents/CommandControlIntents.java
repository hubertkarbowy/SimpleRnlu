package com.hubertkarbowy.simplenlu.intents;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.extractValue;

public class CommandControlIntents {

    static BiFunction<Locale, List<String>, Map<String,String>> openApp = (Locale locale, List<String> args) -> {
        Map<String, String> retVal = new HashMap<>();
        ResourceBundle rb = ResourceBundle.getBundle("responses/CommandControl", locale, new UTF8Control());

        String appName = extractValue("AppName", args);
        List<String> cmd2 = new ArrayList<>();
        cmd2.addAll(Arrays.asList("OK", "VAL:-", "opening", "VAL:"+appName+".")); // TODO: factor out to separate method + catch non-existing property keys
        String spokenText = String.join(" ", cmd2.stream().map(x -> {
            if (x.startsWith("VAL:")) return x.substring(4);
            else return rb.getString(x);
        }).collect(Collectors.toList()));

//        retVal.put("SpokenText", spokenText);
//        retVal.put("DisplayText", spokenText);
        retVal.put("SpokenText", "*DELEGATED*");
        retVal.put("DisplayText", "*DELEGATED*");
        return retVal;
    };

    static BiFunction<Locale, List<String>, Map<String,String>> openWebAddress = (Locale locale, List<String> args) -> {
        Map<String, String> retVal = new HashMap<>();
        ResourceBundle rb = ResourceBundle.getBundle("responses/CommandControl", locale, new UTF8Control());

        String webAddr = extractValue("WebAddressPart", args).replaceAll(" ", "");
        List<String> cmd2 = new ArrayList<>();
        cmd2.addAll(Arrays.asList("OK", "VAL:-", "opening", "VAL:"+webAddr)); // TODO: factor out to separate method + catch non-existing property keys
        String spokenText = String.join(" ", cmd2.stream().map(x -> {
            if (x.startsWith("VAL:")) return x.substring(4);
            else return rb.getString(x);
        }).collect(Collectors.toList()));

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", spokenText);
        return retVal;
    };
}
