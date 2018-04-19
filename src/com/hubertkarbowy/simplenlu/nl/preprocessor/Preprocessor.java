package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Preprocessor {

    protected Map<String, Map<Integer, String>> isyms; // key = transducer name, value = actual isyms map
    protected Map<String, String> isymsPaths;

    public void readSymbols() {
        String symbol;

        try {
            for (Map.Entry<String, String> singleTransducer : isymsPaths.entrySet()) {
                System.out.println("[ISYMSPA]: Reading symbol table of " + singleTransducer.getKey());
                isyms.put(singleTransducer.getKey(), new HashMap<>());
                Map<Integer,String> thisIsyms = isyms.get(singleTransducer.getKey());
                BufferedReader isyms_reader = new BufferedReader(new FileReader(new File(singleTransducer.getValue())));
                Pattern symbolEntry = Pattern.compile("([\\w<>]+)[\\s\\t]+(.*)", Pattern.UNICODE_CHARACTER_CLASS);
                while ((symbol = isyms_reader.readLine()) != null) {
                     System.out.println("SYMBOL LINE "+ symbol);
                    Matcher m = symbolEntry.matcher(symbol);
                    if (m.find()) thisIsyms.put(Integer.parseInt(m.group(2)), m.group(1));
                }
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Map<Integer, String>> getTransducersAndSymbols () {
        return isyms;
    }

    protected Map<String, String> getIsymsPaths () { return isymsPaths; }

    public List<String> unknownize (List<String> nlInput, String transducer) { // TODO: potem zmienic na protected (?), getSymbols tez
        Map<Integer, String> thisIsyms = isyms.get(transducer);
        if (transducer==null || thisIsyms==null) throw new RuntimeException("Transducer " + transducer + " not found!");
        return nlInput.stream().map(x -> thisIsyms.containsValue(x) ? x : "<unk>").collect(Collectors.toList());
    }

    public abstract List<String> tokenize (String asrOutput);
}
