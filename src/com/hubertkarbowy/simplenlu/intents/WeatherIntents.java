package com.hubertkarbowy.simplenlu.intents;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;

public class WeatherIntents {

    static BiFunction<Locale, List<String>, Map<String,String>> weatherResponse = (Locale locale, List<String> args) -> {

        // ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale);
        ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale, new UTF8Control());
        Map<String, String> retVal = new HashMap<>();
        String displayText;
        String spokenText;


        String cityName = extractValue("CityName", args);
        String cityID = fetchOpenWeathermapInfo(cityName);
        if (cityID == null) {
            displayText = spokenText = "Nie udało się znaleźć pogody dla miejscowości " + capitalizeEachWord(cityName);
        }
        else {
            String[] cmd = {"S_in", "location", "VAL:" + capitalizeEachWord(cityName), "willbe", "hot"};
            spokenText = String.join(" ", Arrays.asList(cmd).stream().map(x -> {
                if (x.startsWith("VAL:")) return x.substring(4);
                else return rb.getString(x);
            }).collect(Collectors.toList()));
            displayText = spokenText;
        }


        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", displayText);

        return retVal;
    };
}