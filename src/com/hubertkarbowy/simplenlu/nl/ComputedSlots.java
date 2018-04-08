package com.hubertkarbowy.simplenlu.nl;

import com.hubertkarbowy.simplenlu.intents.IntentHelperMethods;

import java.util.*;
import java.util.function.*;

public class ComputedSlots {

    // Map definitions
    private static Map<String, BiFunction<String, String, String>> plComputations = new HashMap<>();


    // Function definitions
    private static BiFunction<String, String, String> pl_dativeToNom = (x, y) -> { // x = slot value, y = slot type

        // If the slot type is CityName, let's first try lemmatizing with Polimorf in a very simplified way.
        // This will behave erratically since the inputs are underspecified for gender and number, so
        // e.g. for "nowym" we have two nominative forms: nowy (masc) and nowe (neut).
        // However, we're counting on the edit distance on the gazetteer to correct this for us.

        String nominalized = null;
        if (y.equals("CityName")) nominalized = IntentHelperMethods.getPolimorfBaseForm(
                x.substring(0, 1).toUpperCase() + x.substring(1),
                new String[] {"loc", "geograficzna"});
        if (nominalized !=null) return nominalized;

        // Otherwise, try a heuristics.

        String stemcons=""; String sfxending="";
        if (x.endsWith("ie")) {
            if (x.endsWith("dzie")) return x.replaceAll("dzie$", "d");
            else if (x.endsWith("cie")) return x.replaceAll("cie$", "t");
            else return x.replaceAll("ie$", "");
            }
        else if (x.endsWith("dze")) return x.replaceAll("dze$", "ga");
        else if (x.endsWith("niu")) return x.replaceAll("niu$", "ń");
        else if (x.endsWith("u")) return x.replaceAll("u$", "");
        else return x;
        };

    // Maps initialization
    static {
        plComputations.put("NominalizeFromDative", pl_dativeToNom);
    }

    static String compute (String functionName, String slotType, String slotParamValue, Locale culture) {

        String retVal = null;
        BiFunction<String, String, String> f = null;
        switch (culture.toString()) {
            case "en_US":
                // f = enAliases.get(slotName+"+"+slotParamValue);
                break;
            case "pl_PL":
                f = plComputations.get(functionName);
            default:
                break;
        }

        if (f != null) retVal = f.apply(slotParamValue, slotType);
        return retVal;
    }
}