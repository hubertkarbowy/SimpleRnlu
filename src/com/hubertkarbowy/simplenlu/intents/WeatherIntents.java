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

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.*;
import static com.hubertkarbowy.simplenlu.intents.CommonGeo.*;

public class WeatherIntents {

    /* INTENTS AND RESPONSES */

    static BiFunction<Locale, List<String>, Map<String,String>> weatherResponse = (Locale locale, List<String> args) -> {

        Map<String, String> retVal = new HashMap<>();
        String displayText;
        String spokenText;

        String cityName = capitalizeEachWord(extractValue("CityName", args));
        String forWhen = extractValue("NamedDate", args);
        ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale, new UTF8Control());
        String englishName = getEnglishName(cityName, locale);
        CityIDTuple cityIDTuple = disambiguateCityName(englishName);

        if (cityIDTuple == null) {
            displayText = spokenText = rb.getString("nothing_found") + " " + cityName;
        }
        else {
            CityObject cityObject = fetchOpenWeathermapInfo(cityIDTuple, forWhen);
            String returnedDate = cityObject.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            System.out.println("[GAZETTE]: " + cityName + " -> EnglishLookup:" + englishName + " -> CityList:" + cityObject.cityName);
            spokenText = formSpokenResponse_forecast(cityName, returnedDate, cityObject, rb);
            displayText = spokenText;
        }

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", displayText);

        return retVal;
    };

    /* HELPER FUNCTIONS */

    static CityObject fetchOpenWeathermapInfo (CityIDTuple cityIDTuple, String forWhen) {

        if (forWhen==null) forWhen = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        String foundID = cityIDTuple.cityID;
        String foundName = cityIDTuple.cityName;

        if (foundID != null) {
            CityObject retVal = new CommonGeo.CityObject(foundID, foundName);
            String apiResponse = HttpHelperMethods.readFromApi(OPENWEATHERMAP_API_URL, "forecast", new String[] {"APPID="+OPENWEATHERMAP_API_KEY, "id="+retVal.cityID, "units=metric"});
            // String apiResponse = HttpHelperMethods.readResponseFromFile("resources/mocks/openweathermap_mock_metric_athens.json");
            JSONParser parser = new JSONParser();
            JSONObject cityForecast = null;
            try {
                cityForecast = (JSONObject) parser.parse(apiResponse);
                JSONArray forecastList = (JSONArray) cityForecast.get("list");
                JSONObject weatherNow = (JSONObject) forecastList.get(0);
                String returnedDate = extractFromWeatherObject(weatherNow, "DATETIME"); // TODO: Prob can be refactored to remove boilerplate code
                double returnedTemperature = Double.parseDouble(extractFromWeatherObject(weatherNow, "TEMP"));
                double returnedClouds = Double.parseDouble(extractFromWeatherObject(weatherNow, "CLOUDS"));
                double returnedWind = Double.parseDouble(extractFromWeatherObject(weatherNow, "WIND"));

                LocalDate ld = LocalDate.now();
                LocalDate rd = LocalDate.parse(forWhen, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (rd.isAfter(ld)) {
                    for (Object o : forecastList) {
                        JSONObject weatherTomorrow = (JSONObject) o;
                        String thisDate = (String) weatherTomorrow.get("dt_txt");
                        if (thisDate.equals(rd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 12:00:00")) {
                            returnedDate=thisDate;
                            returnedTemperature = Double.parseDouble(extractFromWeatherObject(weatherTomorrow, "TEMP"));
                            returnedClouds = Double.parseDouble(extractFromWeatherObject(weatherTomorrow, "CLOUDS"));
                            returnedWind = Double.parseDouble(extractFromWeatherObject(weatherTomorrow, "WIND"));
                            break;
                        }
                    }
                }
                retVal.dateTime=LocalDateTime.parse(returnedDate, OPENWEATHERMAP_DATETIMEFORMATTER);
                retVal.temperature=returnedTemperature;
                retVal.clouds=returnedClouds;
                retVal.windSpeed=returnedWind;
            }
            catch (ParseException | DateTimeParseException e) {
                throw new RuntimeException(e); // TODO: Dla prod wyrzucic to i wykomentowac return null
                // return null;
            }
            return retVal;
        }
        else return null;
    }

    static String extractFromWeatherObject(JSONObject weatherObject, String what) {
        if (what.equals("DATETIME")) {
            return (String) weatherObject.get("dt_txt");
        }
        else if (what.equals("TEMP")) {
            JSONObject mainObject = (JSONObject) weatherObject.get("main");
            return String.valueOf(mainObject.get("temp"));
        }
        else if (what.equals("CLOUDS")) {
            JSONObject mainObject = (JSONObject) weatherObject.get("clouds");
            return String.valueOf(mainObject.get("all"));
        }
        else if (what.equals("WIND")) {
            JSONObject mainObject = (JSONObject) weatherObject.get("wind");
            return String.valueOf(mainObject.get("speed"));
        }
        else return "0";
    }

    static String formSpokenResponse_forecast(String cityName, String returnedDate, CityObject cityObject, ResourceBundle rb) {
        List<String> cmd2 = new ArrayList<>();
        cmd2.addAll(Arrays.asList("S_in", "location", "VAL:" + capitalizeEachWord(cityName), "itwillbe"));
        if (cityObject.temperature>32.0 || cityObject.temperature<-10.0) cmd2.add("very");
        if (cityObject.temperature<13.0) cmd2.add("cold");
        else if (cityObject.temperature>=13.0 && cityObject.temperature<=27.0) cmd2.add("warm");
        else if (cityObject.temperature>27.0) cmd2.add("hot");
        else cmd2.add("VAL:unk");
        cmd2.addAll(Arrays.asList("VAL:.", "Temperature_C", "willbe_temp", "VAL:"+ roundOffTo1DecPlace(cityObject.temperature), "degrees", "and_a", "sky", "willbe_inf"));
        if (cityObject.clouds<15.0) cmd2.add("clear");
        else if (cityObject.clouds>=15.0 && cityObject.clouds<40) cmd2.add("a_few_clouds");
        else if (cityObject.clouds>=40.0) cmd2.add("cloudy");
        cmd2.add("VAL:.");
        // TODO: 1) Wind info, 2) Round temperature, 3) Move to displayText, 4) Rain info not returned from API?, 5) Add SpokenText

        //String[] cmd = {"S_in", "location", "VAL:" + capitalizeEachWord(cityName), "willbe", "hot", "VAL:for date " + returnedDate + " / " + cityObject.cityID }; // or cityObject.cityName to see gazeteer
        String spokenText = String.join(" ", cmd2.stream().map(x -> {
            if (x.startsWith("VAL:")) return x.substring(4);
            else return rb.getString(x);
        }).collect(Collectors.toList()));
        return spokenText;
    }

    static String roundOffTo1DecPlace(double val) {
        double decimal = val % 1;
        if (decimal > 0 && decimal < 0.25) return ("" + (int)val);
        else if (decimal >=0.25 && decimal <=0.75) return ("" + (int)val + ".5");
        else if (decimal >0.75) return ("" + ((int)val + 1));
        else return "" + val;
    }
}