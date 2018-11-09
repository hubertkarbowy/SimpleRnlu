package com.hubertkarbowy.simplenlu.intents;

import com.hubertkarbowy.simplenlu.util.HttpHelperMethods;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.*;

public class GeneralKnowledgeIntents {

    static Map<String, List<String>> copulae = new HashMap<>();
    static {
        copulae.put("pl_PL", Arrays.asList(new String[] {"–", "jest", "był", "była", "-", "są", "byli", "były"}));
        copulae.put("en_US", Arrays.asList(new String[] {"–", "-", "is", "was", "are", "were"}));
    }

    static BiFunction<Locale, List<String>, Map<String,String>> whereis = (Locale locale, List<String> args) -> {
        Map<String, String> retVal = new HashMap<>();

        String location = extractValue("Location", args);
        String spokenText, displayText;
        displayText = spokenText = generateResponseFromResourceBundle("responses/GeneralKnowledgeResponses", locale, "VAL:"+capitalizeEachWord(location), "is_exsit", "nowhere", "VAL:.");

        retVal.put("SpokenText", spokenText);
        retVal.put("DisplayText", displayText);
        return retVal;
    };

    public static BiFunction<Locale, List<String>, Map<String,String>> whoIs = (Locale locale, List<String> args) -> {
        Map<String, String> retVal = new HashMap<>();
        String persona = capitalizeEachWord(extractValue("Persona", args)).replaceAll(" ", "%20");
        String wikiLng = locale.toString().split("_")[0]; // pl_PL => pl, en_US => en, etc.
        String extractPlain = null;
        String unkResp_D = generateResponseFromResourceBundle("responses/GeneralKnowledgeResponses", locale, "ego", "dont", "know", "who_this_is", "VAL:"+capitalizeEachWord(extractValue("Persona", args)), "VAL:.");
        String unkResp_S = generateResponseFromResourceBundle("responses/GeneralKnowledgeResponses", locale, "ego", "dont", "know", "VAL:.");

        try { // TODO: Factor out to extractArticleFromWikipedia (boolean stripParens)
        String apiResponse = HttpHelperMethods.readFromApi("https://"+wikiLng+".wikipedia.org/w/", "api.php", new String[] {"format=json", "action=query", "prop=extracts", "titles="+persona, "redirects=true"});
        JSONParser parser = new JSONParser();
        JSONObject personaObj = null;

            personaObj = (JSONObject) parser.parse(apiResponse);
            personaObj = (JSONObject) personaObj.get("query");
            personaObj = (JSONObject) personaObj.get("pages");
            for (Object key : personaObj.keySet()) { // just the first key, please.
                JSONObject extracted = (JSONObject) personaObj.get(key);
                extractPlain = (String) extracted.get("extract");
                break;
            }
            extractPlain = extractPlain.replaceAll("<.*?>", "");     // strip html tags
            extractPlain = extractPlain.replaceAll("\\(.*?\\)", ""); // delete everything in parens - too complex for the sents tokenizer!
            extractPlain = extractPlain.replaceAll("/.*?/", "");     // delete IPA transcriptions - they confound the sents tokenizer too...
            extractPlain = extractPlain.replaceAll("(?m)^\\s", "");  // delete empty lines
            if (extractPlain.length()>1000) extractPlain = extractPlain.substring(0,1000); // let's bet the info we need is usually at the beginning - makes no sense to plough the entire text
        }
        catch (ParseException | NullPointerException e) {
            retVal.put("SpokenText", unkResp_S);
            retVal.put("DisplayText", unkResp_D);
        }
        if (extractPlain == null) return retVal;
        System.out.println("[FACTOID]: Extracted and truncated text: " + extractPlain.toString());

        // The actual extraction starts here.

        String[] subjectNames = persona.split("%20");
        BreakIterator it = BreakIterator.getSentenceInstance(locale);
        it.setText(extractPlain);
        int start = it.first();
        try {
            for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
                String sent = extractPlain.substring(start, end);
                System.out.println("[FACTOID]: Processing sentence \"" + sent + "\" with persona=" + Arrays.toString(subjectNames));
                boolean foundName = true;
                int[] subjectNamesEndPos = new int[subjectNames.length];
                for (int i = 0; i < subjectNames.length; i++) {
                    String subjectName = subjectNames[i];
                    int idx = sent.indexOf(subjectName);
                    if (idx == -1) {
                        foundName = false;
                        break;
                    } else subjectNamesEndPos[i] = idx;
                }
                if (foundName == false)
                    continue; // if we haven't found the person's name in the sentence, try in the next one.
                // System.out.println("Found name!" + Arrays.toString(subjectNamesEndPos));
                boolean foundWhoThatIs = true;
                if (foundName == true) {
                    List<String> separators = copulae.get(locale.toString());
                    int idx = separators.stream().map(x -> sent.indexOf(x)).filter(x -> x > -1).findAny().orElse(-1);
                    if (idx == -1) foundWhoThatIs = false;
                    if (idx < subjectNamesEndPos[subjectNames.length - 1])
                        foundWhoThatIs = false; // sanity check: the separator should come after the person's name in the sentence
                    else {
                        StringBuilder sb = new StringBuilder(sent.substring(idx));
                        retVal.put("SpokenText", capitalizeEachWord(extractValue("Persona", args)) + " -" + sb.toString().replaceAll("\n", " "));
                        // If it seems there isn't enough info (because the sentence is too short), let's take the risk and move the BreakIterator
                        // forwards to append the next sentence as well.
                        start = end;
                        end = it.next(); // it's very un-kosher to mess around with iterators inside the loop, but we're guaranteed to exit it anyway, so...?
                        if (end != BreakIterator.DONE & !extractPlain.substring(start, end).replaceAll("\n$", "").contains("\n")) { // get the next sentence unless there's an intervening heading
                            System.out.println("[FACTOID]: Processing second sentence: " + extractPlain.substring(start, end));
                            sb.append(extractPlain.substring(start, end));
                        }
                        retVal.put("DisplayText", capitalizeEachWord(extractValue("Persona", args)) + " -" + (sb.toString()).replaceAll("–", "").replaceAll("\n", " "));
                    }
                }
                if (foundWhoThatIs == true) break;
            }
        }
        catch (Exception e) {
            retVal.put("SpokenText", unkResp_S);
            retVal.put("DisplayText", unkResp_D);
        }
        return retVal;
    };
}
