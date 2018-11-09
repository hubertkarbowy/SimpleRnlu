package com.hubertkarbowy.simplenlu.testconsole;

import com.hubertkarbowy.simplenlu.intents.CommonGeo;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.hubertkarbowy.simplenlu.intents.CommonGeo.disambiguateCityName;
import static com.hubertkarbowy.simplenlu.intents.CommonGeo.getEnglishName;
import static com.hubertkarbowy.simplenlu.intents.GeneralKnowledgeIntents.whoIs;
import static com.hubertkarbowy.simplenlu.nl.slots.ComputedSlots.*;

public class LemmatizerTest {

    public static void evaluate(String fileLoc, String fileNom, String title) throws Exception {

        Locale pl = new Locale("pl", "PL");
        System.out.println(System.getProperty("user.dir"));
        int readLines = 0;
        int correctLemmas = 0;

        try (BufferedReader loc = new BufferedReader(new FileReader(fileLoc));
             BufferedReader nom = new BufferedReader(new FileReader(fileNom)))
        {
            String cityLoc, cityNom;
            while (((cityLoc = loc.readLine()) != null) | (cityNom = nom.readLine()) != null) {
                readLines++;
                String lemmatized = compute("NominalizeFromDative", "CityName", cityLoc, pl);
                System.out.println(cityLoc + " -> EXP=" + cityNom.replaceAll("-", " ") + " ACT=" + lemmatized + (lemmatized.equals(cityNom.replaceAll("-", " ")) ? "" : "[X]"));
                if (lemmatized.equals(cityNom.replaceAll("-", " "))) correctLemmas++;
            }
        }

        System.out.println("Correct lemmas count [" + title + "]: " + correctLemmas + "/" + readLines + " (precision: " + (double)100*correctLemmas/readLines);
    }

    public static void evaluateLevenshtein(String fileLoc, String fileNom, String title) throws Exception {

        Locale pl = new Locale("pl", "PL");
        System.out.println(System.getProperty("user.dir"));
        int readLines = 0;
        int correctLemmas = 0;

        System.out.println("City_loc*City_lem*City_en_resolved*City_en_json*json_object");
        try (BufferedReader loc = new BufferedReader(new FileReader(fileLoc));
             BufferedReader nom = new BufferedReader(new FileReader(fileNom)))
        {
            String cityLoc, cityNom;
            while (((cityLoc = loc.readLine()) != null) | (cityNom = nom.readLine()) != null) {
                readLines++;
                String lemmatized = "";
                String[] allNames = cityLoc.split(" ");
                for (String toLem : allNames) {
                    lemmatized += " " + compute("NominalizeFromDative", "CityName", toLem, pl);
                }
                String englishName = getEnglishName(lemmatized, pl);
                CommonGeo.CityIDTuple cityIDTuple = disambiguateCityName(englishName);
                if (cityIDTuple == null || cityIDTuple.cityName == null) System.out.println(cityLoc+"*"+lemmatized+"*"+englishName+"*NULL*"+"NULL");
                else System.out.println(cityLoc+"*"+lemmatized+"*"+englishName+"*"+cityIDTuple.cityName+"*"+cityIDTuple.toString());
                // if (lemmatized.equals(cityNom)) correctLemmas++;
            }
        }

        // System.out.println("Correct lemmas count [" + title + "]: " + correctLemmas + "/" + readLines + " (precision: " + (double)100*correctLemmas/readLines);
    }

    public static void evaluateWhoIs(String filePers, String title) throws Exception {

        Locale pl = new Locale("pl", "PL");
        int count = 0;

        try (BufferedReader persDb = new BufferedReader(new FileReader(filePers))) {
            String persEntry;
            while ((persEntry = persDb.readLine()) != null) {
                count++;
                if (count % 5 == 0) {
                    Thread.sleep(1000);
                }
                String[] arr = new String[] {"{INTENT:WhoIs}", "<Persona:"+persEntry+">"};
                List<String> lambdaArg = Arrays.asList(arr);
                Map<String, String> m = whoIs.apply(pl, lambdaArg);
                System.out.println(">> " + m.get("DisplayText"));
            }
        }
    }


    public static void main(String[] args) throws Exception {

//        evaluate("testsets/plcapitals_loc.txt", "testsets/plcapitals.txt", "CAPITALS");
//        evaluate("testsets/plcitiespl_loc.txt", "testsets/plcitiespl.txt", "CITIES_30");
//          evaluate("testsets/gorzow_plock_loc.txt", "testsets/gorzow_plock_nom.txt", "CITIES_30");
//        evaluate("testsets/plcitiespl100_loc.txt", "testsets/plcitiespl100.txt", "CITIES_100");
//        System.out.println("********");
        evaluateLevenshtein("testsets/plcitiespl100_loc.txt", "testsets/plcitiespl100.txt", "CITIES_100");

        // ******************************************

        // evaluateWhoIs("testsets/pers99.txt", "WHOIS");



    }
}
