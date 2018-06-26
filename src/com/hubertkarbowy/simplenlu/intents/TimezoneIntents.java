package com.hubertkarbowy.simplenlu.intents;

import com.hubertkarbowy.simplenlu.util.HttpHelperMethods;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.CommonGeo.*;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.OPENWEATHERMAP_API_URL;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.TIMEZONEDB_API_KEY;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.TIMEZONEDB_API_URL;

public class TimezoneIntents {

    static BiFunction<Locale, List<String>, Map<String,String>> timeInLocation = (Locale locale, List<String> args) -> {

        Map<String, String> retVal = new HashMap<>();
        String displayText;
        String spokenText;

        String cityName = capitalizeEachWord(extractValue("CityName", args));
        String forWhen = extractValue("NamedDate", args);
        ResourceBundle rb = ResourceBundle.getBundle("responses/TimezonedbResponses", locale, new UTF8Control());

        String englishName = getEnglishName(cityName, locale);
        CityIDTuple cityIDTuple = disambiguateCityName(englishName);
        String timezonedbResponse = null;

        if (cityIDTuple == null) {
            displayText = spokenText = rb.getString("nothing_found") + " " + cityName;
        }
        else {
            timezonedbResponse = fetchTimezonedb(cityIDTuple);
            System.out.println("[TIMEZON]: " + cityName + " -> " + englishName + " -> " + cityIDTuple.cityName);
            System.out.println("[TIMEZOR]: " + timezonedbResponse);
//            spokenText = timezonedbResponse;
//            displayText = spokenText;
        }
        if (timezonedbResponse == null) {
            spokenText = rb.getString("nothing_found");
            displayText = rb.getString("nothing_found");
        }
        else {
            String[] cmd3 = new String[] {"S_in", "location", "VAL:" + capitalizeEachWord(cityName), "is_now", "VAL:" + timezonedbResponse, "VAL:."};
            displayText = generateResponseFromResourceBundle("responses/TimezonedbResponses", locale, cmd3);
            spokenText = timezonedbResponse;
        }

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", displayText);

        return retVal;
    };

    static String fetchTimezonedb (CityIDTuple cityIDTuple) {

        String lon = cityIDTuple.lon;
        String lat = cityIDTuple.lat;


        if (lon != null && lat != null) {
            String retVal = null;
            String apiResponse = HttpHelperMethods.readFromApi(TIMEZONEDB_API_URL, "get-time-zone", new String[] {"key="+TIMEZONEDB_API_KEY, "format=json", "by=position", "lat="+lat, "lng="+lon});
            // String apiResponse = HttpHelperMethods.readResponseFromFile("resources/mocks/timezonedb_mock.json");
            JSONParser parser = new JSONParser();
            JSONObject timezonedbResponse = null;
            LocalDateTime rd;
            try {
                timezonedbResponse = (JSONObject) parser.parse(apiResponse);
                retVal = timezonedbResponse.get("formatted").toString();
                rd = LocalDateTime.parse(retVal, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            catch (ParseException | DateTimeParseException e) {
                throw new RuntimeException(e);
                // return null;
            }
            return rd.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        else return null;
    }
}
