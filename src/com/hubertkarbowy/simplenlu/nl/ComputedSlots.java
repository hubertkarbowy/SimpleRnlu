package com.hubertkarbowy.simplenlu.nl;

import java.util.*;
import java.util.function.*;

public class ComputedSlots {

    // Map definitions
    private static Map<String, BiFunction<String, String, String>> plComputations = new HashMap<>();

    // Function definitions
    private static BiFunction<String, String, String> pl_dativeToNom = (x, y) -> {
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

    static String compute (String functionName, String slotParamValue, Locale culture) {

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

        if (f != null) retVal = f.apply(slotParamValue, null); // rezerwujemy drugi parametr, ale go nie uzywamy
        return retVal;
    }
}