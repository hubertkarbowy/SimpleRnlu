package com.hubertkarbowy.simplenlu.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class HttpHelperMethods {

    public static String readFromApi(String path, String method, String[] params) {
        String retval=null;

        try {
            String query = path + method + "?" + (String.join("&", params));
            System.out.println("[QUERY   ]: " + query);
            URL obj = new URL(query);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "SimpleRNLU v 0.2");
            int responseCode = con.getResponseCode();
            System.out.println("[QUERYCOD]: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                retval = response.toString();
                in.close();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("[QUERYRES]");
        System.out.println(retval);
        return retval;
    }

    public static String readResponseFromFile(String filePath)
    {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> sb.append(s).append("\n"));
        }
        catch (IOException e) {
            return null;
        }

        return sb.toString();
    }
}
