package com.hubertkarbowy.simplenlu.nl.slots;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AliasedSlots_en_US extends AliasedSlots {
    private static Map<String, Function<String, String>> enAliases = new HashMap<>();

    private static Function<String, String> computeToday = x -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy"));
    private static Function<String, String> computeTomorrow = x -> LocalDate.now().plusDays(1).format((DateTimeFormatter.ofPattern("dd.MM.yyy")));

    static {
        enAliases.put("NamedDate+today", computeToday);
        enAliases.put("NamedDate+tomorrow", computeTomorrow);
    }

    public Map<String, Function<String, String>> getAliases() {
        return enAliases;
    }

}
