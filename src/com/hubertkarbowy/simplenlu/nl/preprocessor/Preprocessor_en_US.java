package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Preprocessor_en_US extends Preprocessor {

    Map<Integer, String> isyms;
    String isymsPath;

    public Map<Integer, String> getSymbols() { return isyms; }
    protected String getIsymsPath() { return isymsPath; }

    public List<String> tokenize (String asrOutput) { // TODO: should be abstract too
        return Arrays.asList(asrOutput.split(" "));
    }

}
