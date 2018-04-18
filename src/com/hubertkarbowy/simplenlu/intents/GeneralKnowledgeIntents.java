package com.hubertkarbowy.simplenlu.intents;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.capitalizeEachWord;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.extractValue;

public class GeneralKnowledgeIntents {

    static BiFunction<Locale, List<String>, Map<String,String>> whereis = (Locale locale, List<String> args) -> {
        Map<String, String> retVal = new HashMap<>();
        ResourceBundle rb = ResourceBundle.getBundle("responses/GeneralKnowledgeResponses", locale, new UTF8Control());

        String location = extractValue("Location", args);
        List<String> cmd2 = new ArrayList<>();
        cmd2.addAll(Arrays.asList("VAL:"+capitalizeEachWord(location), "is_exsit", "nowhere", "VAL:.")); // TODO: factor out to separate method + catch non-existing property keys
        String spokenText = String.join(" ", cmd2.stream().map(x -> {
            if (x.startsWith("VAL:")) return x.substring(4);
            else return rb.getString(x);
        }).collect(Collectors.toList()));

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", spokenText);
        return retVal;
    };
}
