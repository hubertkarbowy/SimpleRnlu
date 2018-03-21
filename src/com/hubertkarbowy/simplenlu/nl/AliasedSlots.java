package com.hubertkarbowy.simplenlu.nl;

import java.util.*;
import java.util.function.*;

public class AliasedSlots {

	private static Map<String, Function<String, String>> plAliases = new HashMap<>();
    private static Map<String, Function<String, String>> enAliases = new HashMap<>();
    private static Function<String, String> computeToday = x -> "13.02.2018";
    private static Function<String, String> computeTomorrow = x -> "14.02.2018";

	static {
	    plAliases.put("NamedDate+dziś", computeToday);
        plAliases.put("NamedDate+jutro", computeTomorrow);

        enAliases.put("NamedDate+today", computeToday);
        enAliases.put("NamedDate+tomorrow", computeTomorrow);
    }

	static String compute (String slotName, String slotParamValue, Locale culture) {

	    String retVal = null;
        Function<String, String> f = null;

	    switch (culture.toString()) {
            case "en_US" :
                f = enAliases.get(slotName+"+"+slotParamValue);
                break;
            case "pl_PL" :
                f = plAliases.get(slotName+"+"+slotParamValue);
            default :
                break;
        }

        if (f != null) retVal = f.apply(slotParamValue);
        return retVal;
	}
}
