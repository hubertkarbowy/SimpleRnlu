package com.hubertkarbowy.simplenlu.intents;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.capitalizeEachWord;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.extractValue;

public class TimezoneIntents {

    static BiFunction<Locale, List<String>, Map<String,String>> timeInLocation = (Locale locale, List<String> args) -> {

        return null;
    };
}
