package com.hubertkarbowy.simplenlu.intents;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import com.hubertkarbowy.simplenlu.nl.MatchedIntent;
import static  com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;

public class IntentFullfilment {

    static Map<String, BiFunction<Locale, List<String>, Map<String, String>> > responses = new HashMap<>(); // this map holds the correspondence between intent names and response functions which generate the response string. E.g. intent name = ShowWeather, response name = a static BiFunction from the WeatherIntents class
    static {
        responses.put("ShowWeather", WeatherIntents.weatherResponse);
        responses.put("WhereIs", GeneralKnowledgeIntents.whereis);
        responses.put("AppLaunch", CommandControlIntents.openApp);
        responses.put("OpenWebAddress", CommandControlIntents.openWebAddress);
        responses.put("WhoIs", GeneralKnowledgeIntents.whoIs);
        responses.put("SetAlarmForTime", CommandControlIntents.setAlarmForTime);
        responses.put("CallTo", CommandControlIntents.callTo);
        responses.put("TimezoneEnquiry", TimezoneIntents.timeInLocation);
    }

    public static String getResponse(MatchedIntent nluOutput, Locale locale) { // both display text and spoken text
        String response = null;
        Map<String, String> texts = null;
        String matchedIntent = nluOutput.getIntent();
        System.out.println("Found intent = " + matchedIntent);
        if (responses.containsKey(matchedIntent)) {
            texts = responses.get(matchedIntent).apply(locale, nluOutput.getSlotsAndValues());
            response = "{SPOKENTEXT:" + formatPunctuation(texts.get("SpokenText")) + "}{DISPLAYTEXT:" + formatPunctuation(texts.get("DisplayText")) +"}";
        }
        else response = "{SPOKENTEXT:Unknown command}{DISPLAYTEXT:Unknown command.}"; // TODO: Externalize strings
        return response;
    }
}
