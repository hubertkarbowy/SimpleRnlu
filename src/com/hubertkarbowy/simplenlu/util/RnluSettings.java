package com.hubertkarbowy.simplenlu.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class RnluSettings {
    public static List<String> availableCultures = Arrays.asList("pl_PL", "en_US");
    public static Path fstRootPath = Paths.get("resources/automata/");
    public static List<String> cmdList = Arrays.asList("/q", "/sc", "/def");
    public static String weatherGazetterPath = "resources/gazetteers/city.list.json";
    public static String polimorfPath = "resources/gazetteers/PoliMorf-0.6.7-locations.tab";

    public static final int MAX_EDIT_DISTANCE = 2; // maximal edit distance between mismatched city names

    public static final String OPENWEATHERMAP_API_KEY="d41602d0873dc3ed5288a05a4d460015";
    public static final String OPENWEATHERMAP_API_URL="http://api.openweathermap.org/data/2.5/";
}
