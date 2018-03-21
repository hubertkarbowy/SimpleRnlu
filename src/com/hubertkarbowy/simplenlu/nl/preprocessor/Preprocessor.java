package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Preprocessor {

    public abstract Map<Integer, String> getSymbols ();
    protected abstract String getIsymsPath ();

    public void readSymbols() {
        String symbol;
        Map<Integer, String> isyms = getSymbols();
        String isymsPath =  getIsymsPath();

        try {
            BufferedReader isyms_reader = new BufferedReader(new FileReader(new File(isymsPath)));
            Pattern symbolEntry = Pattern.compile("([\\w<>]+)[\\s\\t]+(.*)", Pattern.UNICODE_CHARACTER_CLASS);
            while ((symbol = isyms_reader.readLine()) != null) {
                   // System.out.println("SYMBOL LINE "+ symbol);
                    Matcher m = symbolEntry.matcher(symbol);
                    if (m.find()) isyms.put(Integer.parseInt(m.group(2)), m.group(1));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> unknownize (List<String> nlInput) { // TODO: potem zmienic na protected, getSymbols tez
        return nlInput.stream().map(x -> getSymbols().containsValue(x) ? x : "<unk>").collect(Collectors.toList());
    }

    public abstract List<String> tokenize (String asrOutput);
}
