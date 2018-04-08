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
    public static String polimorfPath = "resources/gazetteers/sgjp-20180304.tab";
}
