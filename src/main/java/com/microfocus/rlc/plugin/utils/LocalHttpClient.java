package com.microfocus.rlc.plugin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class LocalHttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalHttpClient.class);

    public static String doGET(String urlString, HashMap<String, String> headers) {
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (headers.size() > 0) {
                headers.forEach(connection::addRequestProperty);
            }
            int responseCode = connection.getResponseCode();
            logger.debug("REST Call Response Code: " + responseCode);
            BufferedReader in;
            if (responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine.trim());
            }
            in.close();
            logger.debug(response.toString());
            return response.toString();
        } catch (MalformedURLException e) {
            logger.error("URL not in valid format: ".concat(e.getMessage()));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static java.lang.String doPOST(String urlString, HashMap<String, String> headers, String requestBody) {
        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (headers.size() > 0) {
                headers.forEach(connection::addRequestProperty);
            }
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] httpBody = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(httpBody, 0, httpBody.length);
                logger.debug("Wrote output stream body. Content length: " + httpBody.length);
            } catch (Exception e) {
                logger.debug("Error writing output stream: " + e.getMessage());
            }

            int responseCode = connection.getResponseCode();
            logger.debug("POST Response Code: " + responseCode);
            if (responseCode < 300) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine.trim());
                }
                in.close();
                logger.debug(response.toString());
                return response.toString();
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine.trim());
                }
                in.close();
                logger.debug("ErrorStream Contents: " + response.toString());
            }
        } catch (Exception e) {
            logger.error("Error in Executing POST Request: " + e.getMessage());
        }
        return null;
    }
}