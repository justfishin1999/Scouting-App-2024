import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StatsQuery {
    public static class TeamsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String teamNumber = getTeamNumberFromPostRequest(exchange);
                String teamData = fetchTeamData(teamNumber);
                // Send the team data to stats_query.html
                String htmlResponse = "<html><head></head><body><div class='team-data-container'>" + teamData + "</div></body></html>";
                sendResponse(exchange, 200, "OK", htmlResponse);
            } else {
                // Unsupported HTTP method
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        private String getTeamNumberFromPostRequest(HttpExchange exchange) throws IOException {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder formData = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                formData.append(line);
            }
            String[] parts = formData.toString().split("=");
            return parts.length > 1 ? parts[1] : "";
        }

        public static String fetchTeamData(String teamNumber) {
            StringBuilder teamData = new StringBuilder();
            try {
                URL url = new URL("https://api.statbotics.io/v3/team/" + teamNumber);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        teamData.append(line);
                    }
                    reader.close();
                    Utils.logMessage("Got data from Statbotics.io for team: " + teamNumber);
                } else {
                    Utils.logMessage("Failed to retrieve data. Response code: " + responseCode);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return parseTeamData(teamData.toString());
        }

        private static String parseTeamData(String json) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            String teamNumber = jsonObject.get("team_number").getAsString();
            String teamName = jsonObject.get("nickname").getAsString();
            String location = jsonObject.get("city").getAsString() + ", " +
                    jsonObject.get("state_prov").getAsString();
            String country = jsonObject.get("country").getAsString();
            return "Team Number: " + teamNumber + "<br>" +
                    "Team Name: " + teamName + "<br>" +
                    "Location: " + location + "<br>" +
                    "Country: " + country;
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String statusMessage, String responseText) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseText.getBytes());
            os.close();
        }
    }

    static class StatsQueryHttp implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if ("GET".equals(requestMethod)) {
                handleGetRequest(exchange);
            } else {
                // Unsupported HTTP method
                sendResponse(exchange, 405, "Method Not Allowed", "Unsupported HTTP method");
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // Read stats_query.html and serve its contents
            String response = readFile("stats_query.html");
            sendResponse(exchange, 200, "OK", response);
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String statusMessage, String responseText) throws IOException {
            exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseText.getBytes());
            os.close();
        }

        private String readFile(String filePath) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(filePath));
                return new String(encoded);
            } catch (IOException e) {
                Utils.logMessage(e.getMessage());
                return "";
            }
        }
    }
}
