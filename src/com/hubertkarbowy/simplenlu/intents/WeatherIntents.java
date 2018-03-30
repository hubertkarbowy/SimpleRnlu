package com.hubertkarbowy.simplenlu.intents;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.extractValue;

public class WeatherIntents {

    static BiFunction<Locale, List<String>, String> weatherResponse = (Locale locale, List<String> args) -> {
        System.out.println(args);
        return "W miejscowości " +  extractValue("CityName", args) + " będzie ciepło."; // todo: localize strings
    };
}
