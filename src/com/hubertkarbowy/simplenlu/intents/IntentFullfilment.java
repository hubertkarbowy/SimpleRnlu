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

    static Map<String, BiFunction<Locale, List<String>, Map<String, String>> > responses = new HashMap<>(); // this map holds the correspondence between intent names and response functions which generate the response string. E.g. intent name = ShowWeather, response name = a static BiFunction from the WeatherIntents class
    static {
        responses.put("ShowWeather", WeatherIntents.weatherResponse);
    }

    public static String getResponse(MatchedIntent nluOutput, Locale locale) { // both display text and spoken text
        String response = null;
        Map<String, String> texts = null;
        String matchedIntent = nluOutput.getIntent();
        System.out.println("Found intent = " + matchedIntent);
        if (responses.containsKey(matchedIntent)) {
            texts = responses.get(matchedIntent).apply(locale, nluOutput.getSlotsAndValues());
            response = "{SPOKENTEXT:" + texts.get("SpokenText") + "}{DISPLAYTEXT:" + texts.get("DisplayText") +"}";
        }
        else response = "{SPOKENTEXT:Unknown command}{DISPLAYTEXT:Unknown command.}";
        return response;
    }
}
