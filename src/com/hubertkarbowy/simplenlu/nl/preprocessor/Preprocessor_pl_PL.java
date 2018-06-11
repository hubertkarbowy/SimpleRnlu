package com.hubertkarbowy.simplenlu.nl.preprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Preprocessor_pl_PL extends Preprocessor {

    public Preprocessor_pl_PL() {
        isyms = new HashMap<>();
        isymsPaths = new HashMap<>();

        try {
            Files.walk(Paths.get("resources/automata/pl_PL/"))
                    .filter(x -> x.toString().contains("isyms.txt"))
                    .forEach(x -> {
                        if (x.getFileName().toString().equals("isyms.txt")) isymsPaths.put("@general", x.toString());
                        else if (x.getFileName().toString().contains("_isyms.txt")) {
                            String transducerName = x.getFileName().toString().replaceAll("_isyms.txt", "");
                            isymsPaths.put(transducerName, x.toString());
                        }
                    });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        readSymbols();
        System.out.println("[ISYMS  ]: "+isyms);
        System.out.println("[ISYMSPA]: "+isymsPaths);
    }

    public List<String> tokenize (String asrOutput) {
        List<String> stopWords = Arrays.asList("proszę");
        String asrOutputAfterStop = Arrays.asList(asrOutput.split(" ")).stream().filter(x -> !stopWords.contains(x)).collect(Collectors.joining(" "));
        List<String> tokens = new ArrayList<>();

        int startPos=0;
        for (int i = 0; i<asrOutputAfterStop.length(); i++) {
          if (asrOutputAfterStop.charAt(i)==' ') {
              tokens.add(asrOutputAfterStop.substring(startPos, i)); // -1 because we don't want the space
              startPos=i+1;
          }
          else if (asrOutputAfterStop.charAt(i)=='.') {
              tokens.add(asrOutputAfterStop.substring(startPos, i)); // -1 because we don't want the dot
              tokens.add(".");
              startPos=i+1;
          }
          else if (asrOutputAfterStop.charAt(i)==':') {
              tokens.add(asrOutputAfterStop.substring(startPos, i)); // -1 because we don't want the colon
              tokens.add(":");
              startPos=i+1;
          }
        }
        if (startPos != asrOutputAfterStop.length()) tokens.add(asrOutputAfterStop.substring(startPos, asrOutputAfterStop.length()));

        // return Arrays.asList(asrOutput.split(" |\\."));
        return tokens;
    }

}
