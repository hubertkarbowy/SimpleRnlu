package com.hubertkarbowy.simplenlu.intents;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hubertkarbowy.simplenlu.nl.MatchedIntent;

public class IntentFullfilment {

    static Map<String, BiFunction<Locale, List<String>, String> > responses = new HashMap<>();
    static {
        responses.put("ShowWeather", WeatherIntents.weatherResponse);
    }

    public static String getResponse(MatchedIntent nluOutput, Locale locale) {
        String response = null;
        String matchedIntent = nluOutput.getIntent();
        System.out.println("Found intent = " + matchedIntent);
        if (responses.containsKey(matchedIntent)) response = responses.get(matchedIntent).apply(locale, nluOutput.getSlotsAndValues());
        else response = "Unknown command.";
        return response;
    }
}
