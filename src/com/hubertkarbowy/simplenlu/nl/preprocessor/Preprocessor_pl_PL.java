package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.util.*;

public class Preprocessor_pl_PL extends Preprocessor {

    public Preprocessor_pl_PL() {
        isyms = new HashMap<>();
        isymsPath = "resources/automata/pl_PL/isyms.txt";
        readSymbols();
    }

    public List<String> tokenize (String asrOutput) { // TODO: should be abstract too
        return Arrays.asList(asrOutput.split(" "));
    } // TODO: Stub

}
