package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.util.*;

public class Preprocessor_pl_PL extends Preprocessor {

    Map<Integer, String> isyms;
    String isymsPath;

    public Preprocessor_pl_PL() {
        isyms = new HashMap<>();
        isymsPath = "resources/automata/pl_PL/isyms.txt";
        readSymbols();
    }

    public Map<Integer, String> getSymbols() { return isyms; } // TODO: zmienic pozniej na protected, w en_US tez
    protected String getIsymsPath() { return isymsPath; }

    public List<String> tokenize (String asrOutput) { // TODO: should be abstract too
        return Arrays.asList(asrOutput.split(" "));
    }

}
