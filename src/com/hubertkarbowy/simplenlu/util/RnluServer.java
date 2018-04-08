package com.hubertkarbowy.simplenlu.util;

import com.hubertkarbowy.simplenlu.intents.IntentHelperMethods;
import com.hubertkarbowy.simplenlu.intents.UTF8Control;
import com.hubertkarbowy.simplenlu.nl.AlignmentHelpers;
import com.hubertkarbowy.simplenlu.nl.SimpleRnlu;
import com.hubertkarbowy.simplenlu.nl.preprocessor.*;

import static com.hubertkarbowy.simplenlu.intents.IntentHelperMethods.getPolimorfBaseForm;
import static com.hubertkarbowy.simplenlu.nl.AlignmentHelpers.editDistance;
import static com.hubertkarbowy.simplenlu.util.FstCompiler.compileRules;
import static com.hubertkarbowy.simplenlu.util.RnluSettings.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class RnluServer {

    Locale pl_PL = new Locale("pl", "PL");
    Preprocessor preprocessor_pl_PL = new Preprocessor_pl_PL(); // TODO: 1) sprawdzenie kultury -> moze mapa z klasami?, 2) zrobic te klasy singletonami
    Preprocessor preprocessor_en_US = new Preprocessor_en_US();
    public static List<String> availableCultures = new ArrayList<>();
    public static Path fstRootPath = Paths.get("resources/automata/");

    static {
        availableCultures.add("pl_PL");
        availableCultures.add("en_US");
    }

    Preprocessor preprocessor;
    Locale locale;
    String asrOutput = "jaka będzie pogoda w krakowie jutro";

    static protected ServerSocket serverSocket = null;
    static protected int serverPort = 55100;


    public static void main(String[] args) {

//        String xx = getPolimorfBaseForm("Nowym", new String[] {"loc", "n2"});
//        System.out.println(xx);
//        HttpHelperMethods.readFromApi(OPENWEATHERMAP_API_URL, "forecast", new String[] {"APPID=d41602d0873dc3ed5288a05a4d460015", "id=6695624"});
//        System.exit(0);

        System.out.println("Starting NLU engine...");
        FstCompiler.compileRules();
        System.out.println("Starting server...");
        try { serverSocket = new ServerSocket(serverPort); }
        catch (IOException e) { throw new RuntimeException("Cannot open port " + serverPort + "", e); }

        Socket cs = null;
        BufferedReader clientRead = null;
        while (true) {
            try {
                cs = serverSocket.accept();
                System.out.println("Accepted");
                new Thread(new SimpleRnlu(cs)).start();
            } catch (Exception e) {
                try {
                    cs.close();
                    clientRead.close();
                } catch (Exception e2) {
                    throw new RuntimeException("Cannot close ", e2);
                }
            }
        }
    }
}
