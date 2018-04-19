package com.hubertkarbowy.simplenlu.util;

import com.hubertkarbowy.simplenlu.nl.preprocessor.Preprocessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


public class FstOutput {

    private List<String> nlTokens = new ArrayList<>();
    private List<String> winningFstOutput = new ArrayList<>();
    private String winningIntent = null;

    private String continuationOf = null; // if this is set, the RNLU will first hit a predefined app context
    private String appContext = null; // if this is set, the RNLU will hit app-specific intents before root ones
    private String fstRootPath = null;
    private String sessionID = null;
    private String sessionPath = null;

    private Preprocessor preprocessor = null;

    private int maxScore = 0;

    public FstOutput (Locale locale,  List<String> nlTokens, String appContext, Preprocessor preprocessor) {
        this.nlTokens = nlTokens;
        this.appContext = appContext; // null means root context
        fstRootPath = "resources/automata/" + locale.toString();
        sessionID = UUID.randomUUID().toString();
        sessionPath = fstRootPath+"/session/utt"+sessionID;
        this.preprocessor = preprocessor;
    }

    private File buildFstFromNlTokens(Path isymsPath, List<String> ppTokens) {
        File commandFile = new File(sessionPath + ".txt");
        StringBuilder fstTxt = new StringBuilder();
        for (int state = 0; state< ppTokens.size(); state++) {
            fstTxt.append(state + "\t" + (state+1) + "\t" + ppTokens.get(state) + "\n");
        }
        fstTxt.append(ppTokens.size());
        // write string to file
        try {
            PrintWriter pw = new PrintWriter(commandFile);
            pw.println(fstTxt.toString());
            pw.close();
            String[] cmd = {"/bin/sh", "-c", "fstcompile --isymbols=" + isymsPath.toString() + " --acceptor " + sessionPath + ".txt " + sessionPath + ".fst"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            commandFile.delete();
        }

        return new File(sessionPath+".fst");
    }

    public void transduce() {

        File nlFst = null;

        try {
        // 1. Sprobuj kontekstowo (jako kontynuacja)
        if (continuationOf != null ) {
            // check the transducers for continuations
            // return if there is a recognized intent
        }

        // 2. Jesli nic - sprobuj dla aplikacji
        if (appContext != null) {
            // check app-specific transducers
            // return if there is a recognized intent
        }

        // 3. Jesli dalej nic - wez ogolny
            List<Path> intents = Files.walk(Paths.get(fstRootPath)).filter(x -> x.toString().contains("/root") && x.toString().endsWith(".fst")).collect(Collectors.toList());
            String[] cmd = new String[3]; cmd[0]="/bin/sh"; cmd[1]="-c";
            for (Path intent : intents) {
                String recognizedIntent = null;
                List<String> fstOutput = new ArrayList<>();
                int currentScore = 0;
                Path isyms = Paths.get(fstRootPath + "/isyms.txt");
                Path osyms = Paths.get(fstRootPath + "/osyms.txt");
                String transducer = "@general";
                String pathWoExtension = intent.toString().replaceAll(".fst", "");
                String transducerBasename = intent.getFileName().toString().replaceAll(".fst", "");
                if (Files.exists(Paths.get(pathWoExtension+"_isyms.txt")) && Files.exists(Paths.get(pathWoExtension+"_osyms.txt"))) { // Choice of symbols table happens here
                    transducer = transducerBasename;
                    isyms = Paths.get(pathWoExtension +"_isyms.txt");
                    osyms = Paths.get(pathWoExtension +"_osyms.txt");

                }
                List<String> ppTokens = preprocessor.unknownize(nlTokens, transducer); // unknownize, given the chosen symbols table
                System.out.println("[PREPROC]: transducer=" + transducer + " " + ppTokens);

                nlFst = buildFstFromNlTokens(isyms, ppTokens);
                cmd[2] = "fstcompose " + nlFst.toString() + " " + intent.toString() + " | fstproject --project_output | fstprint --acceptor";
             //   System.out.println("[FSTCMD attempt]: " + Arrays.asList(cmd).get(2));

                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                String line;
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = in.readLine()) != null) {
                    try {
                        System.out.println(line);
                        String outputTape = line.split("\t")[2];
                        fstOutput.add(outputTape);
                        if (outputTape.startsWith("{INTENT:")) recognizedIntent=outputTape;
                        int partialScore = Integer.parseInt(line.split("\t")[3]);
                        currentScore += partialScore;
                    }
                    catch (ArrayIndexOutOfBoundsException | NumberFormatException ee) {
                        // ignoramus error
                        // System.out.println("[FSTOUT ]: " + fstOutput);
                    }
                }
                in.close();
                if (recognizedIntent!=null) { // TODO: ; STĄD <<= USUŃ I DOPISZ PUNKTY!
                    System.out.println("[FSTCMD ]: " + Arrays.asList(cmd).get(2));
                    System.out.println("[FSTOUT ]: " + fstOutput);
                    if (currentScore > maxScore) {
                        winningFstOutput = fstOutput;
                        maxScore = currentScore;
                    }
                }

                nlFst.delete();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                nlFst.delete();
            }
            catch (NullPointerException ze) {}
        }
    }

    public List<String> getFstOutput() {return winningFstOutput;}
    public String getRecognizedIntent() {return winningIntent;}
}
