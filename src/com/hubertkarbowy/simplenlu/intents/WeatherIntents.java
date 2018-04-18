package com.hubertkarbowy.simplenlu.intents;

import com.hubertkarbowy.simplenlu.nl.AlignmentHelpers;
import com.hubertkarbowy.simplenlu.util.HttpHelperMethods;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;
import static com.hubertkarbowy.simplenlu.nl.AlignmentHelpers.editDistance;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.*;

public class WeatherIntents {

    static class CityIDTuple {
        String cityID;
        String cityName;
        LocalDateTime dateTime;
        double temperature;
        double clouds;
        double windSpeed;

        public CityIDTuple(String cityID, String cityName) {
            this.cityID = cityID;
            this.cityName = cityName;
        }
    }

    /* INTENTS AND RESPONSES */

    static BiFunction<Locale, List<String>, Map<String,String>> weatherResponse = (Locale locale, List<String> args) -> {

        Map<String, String> retVal = new HashMap<>();
        String cityName = capitalizeEachWord(extractValue("CityName", args));
        String forWhen = extractValue("NamedDate", args);
        ResourceBundle rb = ResourceBundle.getBundle("responses/WeatherResponses", locale, new UTF8Control());
        ResourceBundle toEnglishName = ResourceBundle.getBundle("gazetteers/cityNames", locale, new UTF8Control());

        // First we try to substitute locale specific city names with English ones, e.g. Wiedeń -> Vienna. If that fails, we fall back to input values.
        String englishName = null;
        try { englishName = toEnglishName.getString(cityName); }
        catch (MissingResourceException e) {} // nothing needs to be done! Ignoramus error.
        if (englishName==null) englishName=cityName;


        String displayText;
        String spokenText;

        CityIDTuple cityIDTuple = fetchOpenWeathermapInfo(englishName, forWhen);
        if (cityIDTuple == null) {
            displayText = spokenText = rb.getString("nothing_found") + " " + cityName;
        }
        else {
            String returnedDate = cityIDTuple.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            System.out.println("[GAZETTE]: " + cityName + " -> " + englishName + " -> " + cityIDTuple.cityName);
            spokenText = formSpokenResponse_forecast(cityName, returnedDate, cityIDTuple, rb);
            displayText = spokenText;
        }

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", displayText);

        return retVal;
    };

    /* HELPER FUNCTIONS */

    static WeatherIntents.CityIDTuple fetchOpenWeathermapInfo (String cityName, String forWhen) {

        Properties cityNameDisambiguation = new Properties();
        try (FileInputStream disambiguator=new FileInputStream("resources/gazetteers/location_disambiguator.properties")) { cityNameDisambiguation.load(disambiguator); }
        catch (IOException e) { throw new RuntimeException(e); }

        String foundID = null;
        String foundName = null;

        String foundIDHeuristically = null;
        String foundNameHeuristically = null;

        if (forWhen==null) forWhen = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        if (cityNameDisambiguation.getProperty(cityName)!=null) { // Try to disambiguate the most common city names via a lookup table
            foundName = cityName;
            foundID = cityNameDisambiguation.getProperty(cityName);
        }
        else {
            int minEditDistance = 100; // March through the gazetteer if disambiguation failed
            for (Object cityobj : weatherGazetteer) {
                JSONObject city = (JSONObject) cityobj;
                String json_city = city.get("name").toString().toLowerCase();
                String json_cityid = city.get("id").toString();
                int editDistance = editDistance(json_city, cityName.toLowerCase(), AlignmentHelpers.DistanceType.LEVENSHTEIN);
                if (editDistance < minEditDistance) {
                    minEditDistance = editDistance;
                    foundIDHeuristically = json_cityid;
                    foundNameHeuristically = json_city;
                }
                if (editDistance == 0) {
                    foundID = json_cityid;
                    foundName = json_city;
                    break;
                }
            }

            if (foundID==null && minEditDistance<=MAX_EDIT_DISTANCE) {
                foundID=foundIDHeuristically;
                foundName=foundNameHeuristically;
            }
        }

        if (foundID != null) {
            CityIDTuple retVal = new WeatherIntents.CityIDTuple(foundID, foundName);
            // String apiResponse = HttpHelperMethods.readFromApi(OPENWEATHERMAP_API_URL, "forecast", new String[] {"APPID="+OPENWEATHERMAP_API_KEY, "id="+retVal.cityID, "units=metric"});
             String apiResponse = HttpHelperMethods.readResponseFromFile("resources/mocks/openweathermap_mock_metric_athens.json");
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

    static String formSpokenResponse_forecast(String cityName, String returnedDate, CityIDTuple cityIDTuple, ResourceBundle rb) {
        List<String> cmd2 = new ArrayList<>();
        cmd2.addAll(Arrays.asList("S_in", "location", "VAL:" + capitalizeEachWord(cityName), "itwillbe"));
        if (cityIDTuple.temperature>32.0 || cityIDTuple.temperature<-10.0) cmd2.add("very");
        if (cityIDTuple.temperature<13.0) cmd2.add("cold");
        else if (cityIDTuple.temperature>=13.0 && cityIDTuple.temperature<=27.0) cmd2.add("warm");
        else if (cityIDTuple.temperature>27.0) cmd2.add("hot");
        else cmd2.add("VAL:unk");
        cmd2.addAll(Arrays.asList("VAL:.", "Temperature_C", "willbe_temp", "VAL:"+cityIDTuple.temperature, "degrees", "and_a", "sky", "willbe_inf"));
        if (cityIDTuple.clouds<15.0) cmd2.add("clear");
        else if (cityIDTuple.clouds>=15.0 && cityIDTuple.clouds<40) cmd2.add("a_few_clouds");
        else if (cityIDTuple.clouds>=40.0) cmd2.add("cloudy");
        cmd2.add("VAL:.");
        // TODO: 1) Wind info, 2) Round temperature, 3) Move to displayText, 4) Rain info not returned from API?, 5) Add SpokenText

        //String[] cmd = {"S_in", "location", "VAL:" + capitalizeEachWord(cityName), "willbe", "hot", "VAL:for date " + returnedDate + " / " + cityIDTuple.cityID }; // or cityIDTuple.cityName to see gazeteer
        String spokenText = String.join(" ", cmd2.stream().map(x -> {
            if (x.startsWith("VAL:")) return x.substring(4);
            else return rb.getString(x);
        }).collect(Collectors.toList()));
        return spokenText;
    }
}