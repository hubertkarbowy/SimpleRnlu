package com.hubertkarbowy.simplenlu.intents;

import com.hubertkarbowy.simplenlu.nl.AlignmentHelpers;
import com.sun.xml.internal.bind.v2.util.EditDistance;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.util.RnluSettings.polimorfPath;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.weatherGazetterPath;
import static com.hubertkarbowy.simplenlu.nl.AlignmentHelpers.*;

public class IntentHelperMethods {

    static JSONArray weatherGazetteer;
    static Map<String, Map<String, String>> polimorf_pl_PL; // should prob be moved to a locale-specific class
                                                            // keys = 1st column (inflected forms)
                                                            // values = <tags:lexical_category, baseform>
    static {
        try {
            System.out.println("[INFO  ]: Loading weather gazetteer...");
            JSONParser parser = new JSONParser();
            weatherGazetteer = (JSONArray) parser.parse(new FileReader(weatherGazetterPath));
        }
        catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[INFO  ]: Weather gazetteer loaded OK.");
        System.out.println("[INFO  ]: Loading Polimorf...");
        loadPolimorf();
    }

    // iterates over slots and extracts a value
    static String extractValue (String key, List<String> slots) {
        String val = null;
        for (String slot : slots) {
            if (slot.startsWith("<"+key+":")) {
                val = slot.replaceAll("<"+key+":|>", "");
                System.out.println("From " + slot + " extracting " + key + " = " + val);
            }
        }
        return val;
    }

    static String capitalizeEachWord (String seq) {
        return String.join(" ", Arrays.asList(seq.split(" ")).stream().map(x -> x.substring(0,1).toUpperCase() + x.substring(1)).collect(Collectors.toList()));
    }

    static WeatherIntents.CityIDTuple fetchOpenWeathermapInfo (String cityName) {
        String foundID = null;
        String foundName = null;

        String foundIDHeuristically = null;
        String foundNameHeuristically = null;



        int minEditDistance=100;
        for (Object cityobj : weatherGazetteer) {
            JSONObject city = (JSONObject) cityobj;
            String json_city = city.get("name").toString().toLowerCase(); // TODO: może jakiś edit distance?
            String json_cityid = city.get("id").toString();
            int editDistance = editDistance(json_city, cityName, DistanceType.LEVENSHTEIN);
            if (editDistance < minEditDistance) {
                minEditDistance=editDistance;
                foundIDHeuristically = json_cityid;
                foundNameHeuristically = json_city;
            }
            if (editDistance==0) { foundID=json_cityid; foundName=json_city; break; }
        }
        if (foundID != null) {
            return new WeatherIntents.CityIDTuple(foundID, foundName);

        }
        else if (minEditDistance<4) {
            return new WeatherIntents.CityIDTuple(foundIDHeuristically, foundNameHeuristically);
        }
        else return null;
    }

    public static String getPolimorfBaseForm(String inflectedForm, String tags[]) { // for each tag we award one point
        String retVal = null;
        int maxPoints = -1;
        Map<String, String> polimorfEntry = polimorf_pl_PL.get(inflectedForm);
        if (polimorfEntry == null) return null;
        else {
            for (Map.Entry<String, String> form : polimorfEntry.entrySet()) {
                int score = 0;
                String entryTags = form.getKey();
                String entrybaseForm = form.getValue();
                List<String> entryTagsList = Arrays.asList(entryTags.split(":"));
                for (String tag : tags) {
                    if (entryTagsList.contains(tag)) score += 1;
                }
                if (score > maxPoints) {
                    maxPoints = score;
                    retVal = entrybaseForm;
                }
            }
        }
        return retVal;
    }

    static void loadPolimorf() {

        polimorf_pl_PL = new HashMap<>();
        try {
            System.out.println("Restoring polimorf...");
            ObjectInputStream restore = new ObjectInputStream(new FileInputStream("resources/gazetteers/polimorf.obj"));
            System.out.println("OK, done. Now casting...");
            polimorf_pl_PL = (Map<String, Map<String, String>>) restore.readObject();
            System.out.println("Cast OK");

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
//        try (BufferedReader br = new BufferedReader(new FileReader(polimorfPath))) {
//            String line = null;
//            while ((line = br.readLine())!=null) {
//                // if line doesn't contain tabs => continue
//                if (!line.contains("\t")) continue;
//                try {
//                    String[] polimorfEntry = line.split("\t");
//                    String inflectedForm = polimorfEntry[0];
//                    String tags = polimorfEntry[2] + ":" + polimorfEntry[3];
//                    String baseForm = polimorfEntry[1];
//                    // if a key doesn't exist - put it there with a new hashmap
//                    if (!polimorf_pl_PL.containsKey(inflectedForm)) polimorf_pl_PL.put(inflectedForm, new HashMap<>());
//
//                    // next, put the keys (tags) and values (base forms) into the submap
//                    Map<String, String> inflectedKey = polimorf_pl_PL.get(inflectedForm);
//                    inflectedKey.put(tags, baseForm);
//                }
//                catch (ArrayIndexOutOfBoundsException e) {
//                    continue;
//                }
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("Polimorf loaded from " + polimorfPath + " OK.");
//        try {
//            FileOutputStream fs = new FileOutputStream("resources/gazetteers/polimorf.obj");
//            ObjectOutputStream save = new ObjectOutputStream(fs);
//            save.writeObject(polimorf_pl_PL);
//            save.close();
//            fs.close();
//            System.out.println ("OK, saved.");
//        }
//        catch (Exception e) {}

    }
}
