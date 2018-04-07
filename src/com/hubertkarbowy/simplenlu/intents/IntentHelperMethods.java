package com.hubertkarbowy.simplenlu.intents;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.weatherGazetterPath;

public class IntentHelperMethods {

    static JSONArray weatherGazetteer;
    static {
        try {
            JSONParser parser = new JSONParser();
            weatherGazetteer = (JSONArray) parser.parse(new FileReader(weatherGazetterPath));
        }
        catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[INFO  ]: Weather gazetteer OK.");
    }

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

    static String fetchOpenWeathermapInfo (String cityName) {
        String foundID = null;
        for (Object cityobj : weatherGazetteer) {
            JSONObject city = (JSONObject) cityobj;
            String json_city = city.get("name").toString().toLowerCase(); // TODO: może jakiś edit distance?
            String json_cityid = city.get("id").toString();
            if (json_city.equals(cityName)) { foundID=json_cityid; break; }
        }
        return foundID;
    }
}
