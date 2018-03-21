package com.hubertkarbowy.simplenlu.nl;

import com.hubertkarbowy.simplenlu.nl.preprocessor.*;
import com.hubertkarbowy.simplenlu.util.FstOutput;
import jdk.nashorn.internal.parser.JSONParser;

import java.util.*;

public class SimpleRnlu {

    static Map<String, String> clientContext = new HashMap<>();

	public static void main (String[] args) {
		Locale pl_PL = new Locale("pl", "PL");
		Preprocessor preprocessor_pl_PL = new Preprocessor_pl_PL(); // TODO: 1) sprawdzenie kultury -> moze mapa z klasami?, 2) zrobic te klasy singletonami
        Preprocessor preprocessor_en_US = new Preprocessor_en_US();

        Preprocessor preprocessor;
        Locale locale;
        String asrOutput = "jaka jest pogoda dupa w suwonie jutro";

		// to w petli za kazdym przyjsciem nowego komunikatu od klienta
        // przypisanie locale na podstawie komunikatu klienta
        System.out.print("SimpleRnlu initialized. Type NL utterances after the prompt.\n> ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (!(asrOutput=scanner.nextLine()).startsWith("/q")) {
                if (asrOutput.startsWith("/sc")) {
                    setContext(asrOutput.replaceAll("/sc ", ""));
                    System.out.println("Set context: " + clientContext + "\n> ");
                    continue;
                }

                locale = pl_PL;
                switch (locale.toString()) {
                    case "pl_PL":
                        preprocessor = preprocessor_pl_PL;
                        break;
                    // i inne
                    default:
                        preprocessor = preprocessor_en_US;
                }

                System.out.println("[ASR    ]: " + asrOutput);
                List<String> nlTokens = preprocessor.tokenize(asrOutput);
                System.out.println("[TOKENIZ]: " + nlTokens);
                List<String> preprocessorTokens = preprocessor.unknownize(nlTokens);
                System.out.println("[PREPROC]: " + preprocessorTokens);
                FstOutput fst = new FstOutput(locale, preprocessorTokens, null);
                fst.transduce();
                MatchedIntent intentString = new MatchedIntent(nlTokens, fst.getFstOutput(), locale, clientContext);
                System.out.println("[NLU    ]:" + intentString);
                System.out.print("\n> ");
            }
        }
	}

	static void setContext(String scparam) {
	    System.out.println("Param = " + scparam);
	    String[] params = scparam.split(" ");
	    String key = params[0];
	    String value = params[1];
	    clientContext.put(key, value);
    }
}
