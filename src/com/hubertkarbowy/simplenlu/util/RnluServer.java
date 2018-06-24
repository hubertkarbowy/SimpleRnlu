package com.hubertkarbowy.simplenlu.util;

import com.hubertkarbowy.simplenlu.nl.SimpleRnlu;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RnluServer {

    public static List<String> availableCultures = new ArrayList<>();
    static {
        availableCultures.add("pl_PL");
        availableCultures.add("en_US");
    }

    static protected ServerSocket serverSocket = null;
    static protected int serverPort = 55100;


    public static void main(String[] args) {

//        String xx = getPolimorfBaseForm("Nowym", new String[] {"loc", "n2"});
//        System.out.println(xx);
//        HttpHelperMethods.readFromApi(OPENWEATHERMAP_API_URL, "forecast", new String[] {"APPID=d41602d0873dc3ed5288a05a4d460015", "id=6695624"});

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime x = LocalDateTime.parse("2018-04-10 21:00:00", formatter);
//        System.out.println("Day is " + x.format(formatter));
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
