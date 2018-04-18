package com.hubertkarbowy.simplenlu.nl.slots;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;

public abstract class AliasedSlots {

	public static String compute (Map<String, Function<String, String>> aliases, String slotName, String slotParamValue) {

//      System.out.println("??? sn:" + slotName + " spv:" + slotParamValue);
	    String retVal = null;
        Function<String, String> f = null;
        f = aliases.get(slotName+"+"+slotParamValue);

        if (f != null) retVal = f.apply(slotParamValue);
        return retVal;
	}

	public static AliasedSlots getAliasedSlots(Locale culture) {
	    if (culture.toString().equals("pl_PL")) return new AliasedSlots_pl_PL();
	    else if (culture.toString().equals("en_US")) return new AliasedSlots_en_US();
	    else return null;
    }

    public abstract Map<String, Function<String, String>> getAliases();
}