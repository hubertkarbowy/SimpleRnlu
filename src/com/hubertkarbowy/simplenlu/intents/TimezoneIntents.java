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

import static com.hubertkarbowy.simplenlu.intents.CommonGeo.*;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.capitalizeEachWord;
import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.extractValue;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.OPENWEATHERMAP_DATETIMEFORMATTER;

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

        if (cityIDTuple == null) {
            displayText = spokenText = rb.getString("nothing_found") + " " + cityName;
        }
        else {
            String timezonedbResponse = fetchTimezonedb(cityIDTuple);
            System.out.println("[TIMEZON]: " + cityName + " -> " + englishName + " -> " + cityIDTuple.cityName);
            System.out.println("[TIMEZOR]: " + timezonedbResponse);
            spokenText = timezonedbResponse;
            displayText = spokenText;
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
            // String apiResponse = HttpHelperMethods.readFromApi(OPENWEATHERMAP_API_URL, "forecast", new String[] {"APPID="+OPENWEATHERMAP_API_KEY, "id="+retVal.cityID, "units=metric"});
            String apiResponse = HttpHelperMethods.readResponseFromFile("resources/mocks/timezonedb_mock.json");
            JSONParser parser = new JSONParser();
            JSONObject timezonedbResponse = null;
            try {
                timezonedbResponse = (JSONObject) parser.parse(apiResponse);
                retVal = timezonedbResponse.get("formatted").toString();
                // LocalDate rd = LocalDate.parse(retVal, DateTimeFormatter.ISO_DATE_TIME);
            }
            catch (ParseException | DateTimeParseException e) {
                throw new RuntimeException(e); // TODO: Dla prod wyrzucic to i wykomentowac return null
                // return null;
            }
            return retVal;
        }
        else return null;
    }
}
