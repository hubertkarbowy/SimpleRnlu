package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.util.*;

public class Preprocessor_en_US extends Preprocessor {

    public Preprocessor_en_US() {
        isyms = new HashMap<>();
        isymsPaths = new HashMap<>();
        readSymbols();
    }

    public List<String> tokenize (String asrOutput) { // TODO: should be abstract too
        return Arrays.asList(asrOutput.split(" "));
    } // TODO: Stub/q
}
