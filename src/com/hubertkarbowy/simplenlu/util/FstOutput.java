package com.hubertkarbowy.simplenlu.util;

import java.io.*;
import java.util.*;

public class FstOutput {

    private List<String> ppTokens = new ArrayList<>();
    private List<String> fstOutput = new ArrayList<>();
    private String recognizedIntent = null;

    private String continuationOf = null; // if this is set, the RNLU will first hit a predefined app context
    private String appContext = null; // if this is set, the RNLU will hit app-specific intents before root ones
    private String fstRootPath = null;
    private String sessionID = null;
    private String sessionPath = null;

    public FstOutput (Locale locale, List<String> preprocessorOutput, String appContext) {
        ppTokens = preprocessorOutput;
        this.appContext = appContext; // null means root context
        fstRootPath = "resources/automata/" + locale.toString();
        sessionID = UUID.randomUUID().toString();
        sessionPath = fstRootPath+"/session/utt"+sessionID;
    }

    private File buildFstFromNlTokens() {
        File commandFile = new File(sessionPath + ".txt");
        StringBuilder fstTxt = new StringBuilder();
        // List<File> compiledRules = Arrays.stream(new File(fstRootPath+"/"+appContext).listFiles()).filter(x->x.toString().endsWith("fst")).collect(Collectors.toList()); // don't use streams - no guarantee on order
        for (int state = 0; state< ppTokens.size(); state++) {
            fstTxt.append(state + "\t" + (state+1) + "\t" + ppTokens.get(state) + "\n");
        }
        fstTxt.append(ppTokens.size());
        // write string to file
        try {
            PrintWriter pw = new PrintWriter(commandFile);
            pw.println(fstTxt.toString());
            pw.close();
            String[] cmd = {"/bin/sh", "-c", "fstcompile --isymbols=" + fstRootPath + "/isyms.txt --acceptor " + sessionPath + ".txt " + sessionPath + ".fst"};
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

        File nlFst = buildFstFromNlTokens();

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


            String[] cmd = {"/bin/sh", "-c", "fstcompose " + nlFst.toString() + " " + fstRootPath + "/root/pogoda.fst " + " | fstproject --project_output | fstprint --acceptor"};
            Process p = Runtime.getRuntime().exec(cmd);
            System.out.println("[FSTCMD ]: " + Arrays.asList(cmd).get(2));
            p.waitFor();
            String line;
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                try {
                   // System.out.println(line);
                    String outputTape = line.split("\t")[2];
                    fstOutput.add(outputTape);
                    if (outputTape.startsWith("{INTENT:")) recognizedIntent=outputTape;
                }
                catch (ArrayIndexOutOfBoundsException ee) {
                    System.out.println("[FSTOUT ]: " + fstOutput);
                }
            }
            in.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            nlFst.delete();
        }

//        recognizedIntent = "{INTENT:ShowWeather}";
//        fstOutput.add("<KW>"); fstOutput.add("<KW>");
//        fstOutput.add("<KW>"); fstOutput.add("{INTENT:ShowWeather}");  fstOutput.add("<KW>");
//        fstOutput.add("<COMPUTED_SLOT:CityName:NominalizeFromDative>"); fstOutput.add("<LOCALE_ALIAS_COMPUTED_SLOT:NamedDate>");
    }

    public List<String> getPpTokens() {return ppTokens;}
    public List<String> getFstOutput() {return fstOutput;}
    public String getRecognizedIntent() {return recognizedIntent;}
}
