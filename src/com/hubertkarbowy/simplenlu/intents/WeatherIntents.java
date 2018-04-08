package com.hubertkarbowy.simplenlu.intents;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;

public class WeatherIntents {

    static class CityIDTuple {
        String cityID;
        String cityName;
        LocalDateTime dateTime;
        double temperature;
        double windSpeed;

        public CityIDTuple(String cityID, String cityName) {
            this.cityID = cityID;
            this.cityName = cityName;
        }
    }

    static BiFunction<Locale, List<String>, Map<String,String>> weatherResponse = (Locale locale, List<String> args) -> {

        // ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale);
        String cityName = extractValue("CityName", args);
        ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale, new UTF8Control());
        ResourceBundle toEnglishName = ResourceBundle.getBundle("gazetteers/cityNames", locale, new UTF8Control());

        // First we try to substitute locale specific city names with English ones, e.g. Wiedeń -> Vienna. If that fails, we fall back to input values.
        String englishName = null;
        try { englishName = toEnglishName.getString(cityName); }
        catch (MissingResourceException e) {} // nothing needs to be done!
        if (englishName==null) englishName=cityName;

        Map<String, String> retVal = new HashMap<>();
        String displayText;
        String spokenText;

        CityIDTuple cityIDTuple = fetchOpenWeathermapInfo(englishName);
        if (cityIDTuple == null) {
            displayText = spokenText = rb.getString("nothing_found") + " " + capitalizeEachWord(cityName);
        }
        else {
            System.out.println("[GAZETTE]: " + cityName + " -> " + englishName + " -> " + cityIDTuple.cityName);
            String[] cmd = {"S_in", "location", "VAL:" + capitalizeEachWord(cityName), "willbe", "hot"}; // or cityIDTuple.cityName to see gazeteer
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