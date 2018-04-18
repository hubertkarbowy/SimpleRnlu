package com.hubertkarbowy.simplenlu.nl.slots;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AliasedSlots_pl_PL extends AliasedSlots {
    private static Map<String, Function<String, String>> plAliases = new HashMap<>();

    private static Function<String, String> computeToday = x -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyy"));
    private static Function<String, String> computeTomorrow = x -> LocalDate.now().plusDays(1).format((DateTimeFormatter.ofPattern("dd.MM.yyy")));

    static {
        plAliases.put("NamedDate+dziś", computeToday);
        plAliases.put("NamedDate+jutro", computeTomorrow);
    }

    public Map<String, Function<String, String>> getAliases() {
        return plAliases;
    }
}
