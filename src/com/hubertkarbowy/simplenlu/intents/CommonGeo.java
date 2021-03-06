package com.hubertkarbowy.simplenlu.intents;

import com.hubertkarbowy.simplenlu.nl.AlignmentHelpers;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.weatherGazetteer;
import static com.hubertkarbowy.simplenlu.nl.AlignmentHelpers.editDistance;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.MAX_EDIT_DISTANCE;

public class CommonGeo {

    static class CityObject {
        String cityID;
        String cityName;
        LocalDateTime dateTime;
        double temperature;
        double clouds;
        double windSpeed;

        public CityObject(String cityID, String cityName) {
            this.cityID = cityID;
            this.cityName = cityName;
        }
    }

    public static class CityIDTuple {
        String cityID; public String cityName; String lon; String lat; String country;
        public CityIDTuple(String cityID, String cityName) { this.cityID = cityID; this.cityName = cityName; }
        public CityIDTuple(String cityID, String cityName, String lon, String lat) { this.cityID = cityID; this.cityName = cityName; this.lon = lon; this.lat = lat; }
        public CityIDTuple(String cityID, String cityName, String lon, String lat, String country) { this.cityID = cityID; this.cityName = cityName; this.lon = lon; this.lat = lat; this.country = country;}

        @Override
        public String toString() {
            return "city="+cityID+";lat="+lat+";lon="+lon+";country="+country;
        }
    }

    public static String getEnglishName(String cityName, Locale locale) {
        ResourceBundle toEnglishName = ResourceBundle.getBundle("gazetteers/cityNames", locale, new UTF8Control());

        // Try to substitute locale specific city names with English ones, e.g. Wiedeń -> Vienna. If that fails, we fall back to input values.
        String englishName = null;
        try { englishName = toEnglishName.getString(cityName); }
        catch (MissingResourceException e) {} // nothing needs to be done! Ignoramus error.
        if (englishName==null) englishName=cityName;

        return englishName;
    }

    public static CityIDTuple disambiguateCityName(String englishCityName) {

        String foundID = null;
        String foundName = null;
        String foundCountry = null;
        String foundLon = null;
        String foundLat = null;

        String foundIDHeuristically = null;
        String foundNameHeuristically = null;
        String foundCountryHeuristically = null;
        String foundLonHeuristically = null;
        String foundLatHeuristically = null;

        Properties cityNameDisambiguation = new Properties();
        try (FileInputStream disambiguator=new FileInputStream("resources/gazetteers/location_disambiguator.properties")) {
            cityNameDisambiguation.load(disambiguator);
        }
        catch (IOException e) { throw new RuntimeException(e); }

        if (cityNameDisambiguation.getProperty(englishCityName) != null) { // Try to disambiguate the most common city names via a lookup table
            foundName = englishCityName;
            String[] cityRecord = cityNameDisambiguation.getProperty(englishCityName).split("\\*");
            foundID = cityRecord[0]; foundLon = cityRecord[1]; foundLat = cityRecord[2]; foundCountry = "LOOKUP";
        }
        else {
            int minEditDistance = 100; // March through the gazetteer if disambiguation failed
            for (Object cityobj : weatherGazetteer) {
                JSONObject city = (JSONObject) cityobj;
                String json_city = city.get("name").toString().toLowerCase();
                String json_cityid = city.get("id").toString();
                String json_country = city.get("country").toString();
                JSONObject coordinates = (JSONObject) city.get("coord");
                String json_lon = coordinates.get("lon").toString();
                String json_lat = coordinates.get("lat").toString();
                int editDistance = editDistance(json_city, englishCityName.toLowerCase(), AlignmentHelpers.DistanceType.LEVENSHTEIN);
                if (editDistance < minEditDistance) {
                    minEditDistance = editDistance;
                    foundIDHeuristically = json_cityid;
                    foundNameHeuristically = json_city;
                    foundLatHeuristically = json_lat;
                    foundLonHeuristically = json_lon;
                    foundCountryHeuristically = json_country;
                }
                if (editDistance == 0) {
                    foundID = json_cityid;
                    foundName = json_city;
                    foundLat = json_lat;
                    foundLon = json_lon;
                    foundCountry = json_country;
                    break;
                }
            }

            if (foundID==null && minEditDistance<=MAX_EDIT_DISTANCE) {
                foundID=foundIDHeuristically;
                foundName=foundNameHeuristically;
                foundLat=foundLatHeuristically;
                foundLon=foundLonHeuristically;
                foundCountry=foundCountryHeuristically;
            }
        }
        if (foundID == null || foundName == null) return null;
        else return new CityIDTuple(foundID, foundName, foundLon, foundLat, foundCountry);
    }
}
