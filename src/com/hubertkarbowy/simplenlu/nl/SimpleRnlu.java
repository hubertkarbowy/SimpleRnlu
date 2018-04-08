package com.hubertkarbowy.simplenlu.nl;

import com.hubertkarbowy.simplenlu.intents.IntentFullfilment;
import com.hubertkarbowy.simplenlu.nl.preprocessor.*;
import com.hubertkarbowy.simplenlu.util.FstOutput;
import com.hubertkarbowy.simplenlu.util.FstCompiler;
import com.hubertkarbowy.simplenlu.util.StopServerException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SimpleRnlu implements Runnable {

    static String version = "0.2a";
    private Map<String, String> clientContext = new HashMap<>();
    Preprocessor preprocessor;
    Locale locale;

    Socket clientSocket = null;
    BufferedReader clientRead = null;
    PrintStream clientWrite = null;

    public SimpleRnlu (Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        clientRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientWrite = new PrintStream(clientSocket.getOutputStream());
    }

//	public static void main (String[] args) {
//		Locale pl_PL = new Locale("pl", "PL");
//		Preprocessor preprocessor_pl_PL = new Preprocessor_pl_PL(); // TODO: 1) sprawdzenie kultury -> moze mapa z klasami?, 2) zrobic te klasy singletonami
//        Preprocessor preprocessor_en_US = new Preprocessor_en_US();
//
//        Preprocessor preprocessor;
//        Locale locale;
//        String asrOutput = "jaka będzie pogoda w krakowie jutro";
//        FstCompiler.compileRules();
//
//		// to w petli za kazdym przyjsciem nowego komunikatu od klienta
//        // przypisanie locale na podstawie komunikatu klienta
//        System.out.print("SimpleRnlu initialized. Type NL utterances after the prompt.\n> ");
//        try (Scanner scanner = new Scanner(System.in)) {
//            while (!(asrOutput=scanner.nextLine()).startsWith("/q")) {
//                if (asrOutput.startsWith("/sc")) {
//                    setContext(asrOutput.replaceAll("/sc ", ""));
//                  //  System.out.println("Set context: " + clientContext + "\n> ");
//                    continue;
//                }
//                if (asrOutput.startsWith("/def")) {
//                    PredefinedStates.setDefaultContext();
//                    continue;
//                }
//                if (asrOutput.startsWith("/cc")) {
//                    clearContext();
//                    continue;
//                }
//
//                locale = pl_PL;
//                switch (locale.toString()) {
//                    case "pl_PL":
//                        preprocessor = preprocessor_pl_PL;
//                        break;
//                    // i inne
//                    default:
//                        preprocessor = preprocessor_en_US;
//                }
//
//                System.out.println("[ASR    ]: " + asrOutput);
//                List<String> nlTokens = preprocessor.tokenize(asrOutput);
//                System.out.println("[TOKENIZ]: " + nlTokens);
//                List<String> preprocessorTokens = preprocessor.unknownize(nlTokens);
//                System.out.println("[PREPROC]: " + preprocessorTokens);
//                FstOutput fst = new FstOutput(locale, preprocessorTokens, null);
//                fst.transduce();
//                MatchedIntent intentString = new MatchedIntent(nlTokens, fst.getFstOutput(), locale, clientContext);
//                System.out.println("[NLU    ]:" + intentString);
//                System.out.print("\n> ");
//            }
//        }
//	}

	public void run() {
        System.out.println("Reading from client ...");
        try {
            clientWrite.println("SimpleRNLU server v." + version);
            clientWrite.println("Protocol:\n1st line: culture, e.g. pl_PL");
            String recvd = clientRead.readLine();
            System.out.println("Culture received: " + recvd);
                if (recvd.equals("pl_PL")) { locale = new Locale("pl", "PL"); preprocessor = new Preprocessor_pl_PL(); }
                else if (recvd.equals("en_US")) { locale = new Locale("en", "US"); preprocessor = new Preprocessor_en_US(); }
                else {clientWrite.println("E:Unsupported culture."); clientSocket.close(); return;}

            // MAIN INTERPRETER
            while (!(recvd=clientRead.readLine()).startsWith("/q")) {
                try {
                    System.out.println("Recvd: " + recvd);
                    if (recvd.startsWith("/sc")) {
                        setContext(recvd.replaceAll("/sc ", ""));
                        //  System.out.println("Set context: " + clientContext + "\n> ");
                    } else if (recvd.startsWith("/def")) PredefinedStates.setDefaultContext(this);
                    else if (recvd.startsWith("/cc")) clearContext();
                    else if (recvd.startsWith("/cmd ")) {
                        String asrOutput = recvd.replaceAll("/cmd ", "");
                        asrOutput = asrOutput.toLowerCase();
                        System.out.println("[ASR    ]: " + asrOutput);
                        List<String> nlTokens = preprocessor.tokenize(asrOutput);
                        System.out.println("[TOKENIZ]: " + nlTokens);
                        List<String> preprocessorTokens = preprocessor.unknownize(nlTokens);
                        System.out.println("[PREPROC]: " + preprocessorTokens);
                        FstOutput fst = new FstOutput(locale, preprocessorTokens, null);
                        fst.transduce();
                        MatchedIntent intentString = new MatchedIntent(nlTokens, fst.getFstOutput(), locale, clientContext);
                        System.out.println("[NLU    ]:" + intentString);
                        String responses = IntentFullfilment.getResponse(intentString, locale);
                        System.out.println("[RESPONS]:" + responses);
                        clientWrite.println(intentString);
                        clientWrite.println(responses);
                    } else {
                        System.out.println("Unknown command");
                        clientWrite.println("Unknown command");
                    }
                }
                catch (RuntimeException ex) {
                    clientWrite.println("Oops... sth went wrong");
                    ex.printStackTrace();
                }
            }
            clientSocket.close();
        }
        catch (Exception e) {
            throw new RuntimeException("Error in multithreading ", e);
        }
    }

	protected void setContext(String scparam) {
	    System.out.println("Param = " + scparam);
	    String[] params = scparam.split("\\*");
	    String key = params[0];
	    String value = params[1];
	    this.clientContext.put(key, value);
    }

    private void clearContext() {
        clientContext.clear();
    }
}
