package com.hubertkarbowy.simplenlu.nl.slots;

import com.hubertkarbowy.simplenlu.intents.IntentHelperMethods;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ComputedSlots {

    // Map definitions
    private static Map<String, BiFunction<String, String, String>> plComputations = new HashMap<>();


    // Function definitions
    private static BiFunction<String, String, String> pl_locativeToNom = (x, y) -> { // x = slot value, y = slot type

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

        String[] locationTokens = x.split(" ");
        String resultantLocation = "";
        for (String token : locationTokens) {
            if (token.endsWith("ie")) {
                if (token.endsWith("dzie")) resultantLocation = resultantLocation + " " + token.replaceAll("dzie$", "d");
                else if (token.endsWith("cie")) resultantLocation = resultantLocation + " " + token.replaceAll("cie$", "t");
                else resultantLocation = resultantLocation + " " + token.replaceAll("ie$", "");
            }
            else if (token.endsWith("dze")) resultantLocation = resultantLocation + " " + token.replaceAll("dze$", "ga");
            else if (token.endsWith("niu")) resultantLocation = resultantLocation + " " + token.replaceAll("niu$", "ń");
            else if (token.endsWith("u")) resultantLocation = resultantLocation + " " + token.replaceAll("u$", "");
            else if (token.endsWith("im")) resultantLocation = resultantLocation + " " + token.replaceAll("im$", "i");
            else if (token.endsWith("ej")) resultantLocation = resultantLocation + " " +  token.replaceAll("ej$", "a");
            // else if (token.endsWith("nej")) resultantLocation = resultantLocation + " " + token.replaceAll("nej$", "a");
            // else if (token.endsWith("łej")) resultantLocation = resultantLocation + " " + token.replaceAll("łej$", "a");
            else if (token.endsWith("nym")) resultantLocation = resultantLocation + " " + token.replaceAll("nym$", "ne");
            else resultantLocation = resultantLocation + " " + token;
        }
        return resultantLocation.trim();
        };

    private static BiFunction<String, String, String> pl_appNameMapperFromAcc = (x, y) -> {
        if (x.equals("galerię")) return "Gallery";
        else if (x.equals("kalkulator")) return "Calculator";
        else if (x.equals("kontakty")) return "Contacts";
        else if (x.equals("ustawienia")) return "Settings";
        else if (x.equals("sieci")) return "_Net";
        else if (x.equals("zegar")) return "Clock";
        else if (x.equals("przeglądarkę")) return "Browser";
        else if (x.equals("pocztę") || x.equals("email") || x.equals("e-mail")) return "Email";
        else if (x.equals("muzykę")) return "MusicPlayer";
        else if (x.equals("telefon")) return "Phone";
        else if (x.equals("smsy")) return "Sms";
        else if (x.equals("nawigację")) return "Navigation";
        else if (x.equals("alarmy")) return "Alarms _ShowAll";
        else return x;

    };

    private static BiFunction<String, String, String> all_Capitalize = (x,y) -> {
        String[] words = x.split(" ");
        List<String> capitalizedList = Arrays.asList(words).stream().map(w -> w.substring(0,1).toUpperCase() + w.substring(1)).collect(Collectors.toList());
        String capitalizedX = String.join(" ", capitalizedList);
        return capitalizedX;
    };

    private static BiFunction<String, String, String> pl_stempelStem = (x,y) -> {
        String[] words = x.split(" ");
        List<String> stemmedList = Arrays.asList(words).stream().map(w -> IntentHelperMethods.getPolishStem(w)).collect(Collectors.toList());
        String stemmedPhrase = String.join(" ", stemmedList);
        return stemmedPhrase;
    };

    // Maps initialization
    static {
        plComputations.put("NominalizeFromDative", pl_locativeToNom);
        plComputations.put("GetAppFromAcc", pl_appNameMapperFromAcc);
        plComputations.put("Capitalize", all_Capitalize);
        plComputations.put("Stem", pl_stempelStem);
    }

    public static String compute (String functionName, String slotType, String slotParamValue, Locale culture) {

        String retVal = null;
        BiFunction<String, String, String> f = null;
        switch (culture.toString()) {
            case "en_US":
                // f = enAliases.get(slotName+"+"+slotParamValue);
                f = plComputations.get(functionName);
                break;
            case "pl_PL":
                f = plComputations.get(functionName);
                break;
            default:
                break;
        }

        if (f != null) retVal = f.apply(slotParamValue, slotType);
        return retVal;
    }
}