import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;

public class StatsQuery {
    public static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Call the API to fetch match data
                String matchData = fetchMatchData();

                // Write the match data to matches.html
                writeMatchesHtml(matchData);

                // Send matches.html to the client
                sendMatchesHtmlToClient(exchange);
            } else {
                // Unsupported HTTP method
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        public static String fetchMatchData() {
            StringBuilder matchData = new StringBuilder();
            try {
                // Statbotics.io API key
                String apiKey = Constants.PasswordConstants.APIKEY;
                // Event key for the event you want to fetch matches for
                String eventKey = Constants.TBA_API.TBA_EVENT;

                // Create URL object
                URL url = new URL("https://api.statbotics.io/v2/matches/event/"+Constants.TBA_API.TBA_EVENT);

                // Open connection
                Utils.logMessage("Attempting API connection to: " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set request properties
                connection.setRequestMethod("GET");
                //connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("User-Agent", "Java-App-1.0");

                // Get response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        matchData.append(line);
                    }
                    reader.close();
                    Utils.logMessage("Got data from Statbotics.io using API key: " + eventKey);
                } else {
                    Utils.logMessage("Failed to retrieve data. Response code: " + responseCode);
                }

                // Close connection
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return matchData.toString();
        }

        public static void writeMatchesHtml(String matchData) {
            try (PrintWriter htmlContent = new PrintWriter(new FileWriter("stats_query.html"))) {
                // Parse JSON response
                JsonArray matchesArray = JsonParser.parseString(matchData).getAsJsonArray();

                // Write HTML content for matches
                htmlContent.append("<html><head><title>Match Data</title>");
                htmlContent.append("<link rel=\"stylesheet\" href=\"style.css\">");
                htmlContent.append("</head><body>");
                htmlContent.append("<div class='navbar'>");
                htmlContent.append("<a href='/'>Home</a>");
                htmlContent.append("<a href='/pit-scout.html'>Pits</a>");
                htmlContent.append("<a href='/team_averages.html'>Team Averages</a>");
                htmlContent.append("<a href='/actual_stats.html'>Team Data</a>");
                htmlContent.append("<a href='/reports.html'>Reports</a>");
                htmlContent.append("<a href='/teams.html'>Teams</a>");
                htmlContent.append("<a class='active' href='/stats_query.html'>Stats Query</a>");
                htmlContent.append("<a href='/admin.html'>Admin</a>");
                htmlContent.append("<div class='clock' id='clock'></div>");
                htmlContent.append("</div><div class='container'>");
                htmlContent.append("<h1>Match List</h1>");
                htmlContent.append("<center><p>Showing info from <i><a href='https://statbotics.io/'>Statbotics.io</a></i> for event ID: " + Constants.TBA_API.TBA_EVENT + "</p></center>");
                htmlContent.append("<table class='table-test'>");
                htmlContent.append("<tr><th>Match Key</th><th>Match Number</th><th>Red Team Numbers</th><th>Red EPA</th><th>Red RP 1 Prob</th><th>Red RP 2 Prob</th><th>Blue Team Numbers</th><th>Blue EPA</th><th>Blue RP 1 Prob</th><th>Blue RP 2 Prob</th></tr>");

                Gson gson = new Gson();
                for (JsonElement matchElement : matchesArray) {
                    JsonObject matchObject = matchElement.getAsJsonObject();
                    String matchKey = extractAfterEventKey(matchObject.get("key").getAsString(),Constants.TBA_API.TBA_EVENT+"_");
                    String matchNumber = matchObject.get("match_number").getAsString();
                    String redTeamNumbers = matchObject.get("red_1").getAsString() + ", " + matchObject.get("red_2").getAsString() + ", " + matchObject.get("red_3").getAsString();
                    String redEPA = matchObject.get("red_epa_sum").getAsString();
                    String redRP1Prob = formatPercentage(matchObject.get("red_rp_1_prob").getAsDouble());
                    String redRP2Prob = formatPercentage(matchObject.get("red_rp_2_prob").getAsDouble());
                    String blueTeamNumbers = matchObject.get("blue_1").getAsString() + ", " + matchObject.get("blue_2").getAsString() + ", " + matchObject.get("blue_3").getAsString();
                    String blueEPA = matchObject.get("blue_epa_sum").getAsString();
                    String blueRP1Prob = formatPercentage(matchObject.get("blue_rp_1_prob").getAsDouble());
                    String blueRP2Prob = formatPercentage(matchObject.get("blue_rp_2_prob").getAsDouble());

                    htmlContent.append("<tr>");
                    htmlContent.append("<td>").append(matchKey).append("</td>");
                    htmlContent.append("<td>").append(matchNumber).append("</td>");
                    htmlContent.append("<td>").append(redTeamNumbers).append("</td>");
                    htmlContent.append("<td>").append(redEPA).append("</td>");
                    htmlContent.append("<td>").append(redRP1Prob).append("</td>");
                    htmlContent.append("<td>").append(redRP2Prob).append("</td>");
                    htmlContent.append("<td>").append(blueTeamNumbers).append("</td>");
                    htmlContent.append("<td>").append(blueEPA).append("</td>");
                    htmlContent.append("<td>").append(blueRP1Prob).append("</td>");
                    htmlContent.append("<td>").append(blueRP2Prob).append("</td>");
                    htmlContent.append("</tr>");
                }

                htmlContent.append("</table></div>");
                htmlContent.append("<script src='script-no-pwd.js'></script>");
                htmlContent.append("</body></html>");
            } catch (IOException e) {
                Utils.logMessage(e.getMessage());
            }
        }

        private static String formatPercentage(double value) {
            return String.format("%.2f%%", value * 100);
        }
        
        public static String extractAfterEventKey(String input, String eventKey) {
            int index = input.indexOf(eventKey);
            if (index != -1) {
                return input.substring(index + eventKey.length());
            }
            return "";
        }



        private static void sendMatchesHtmlToClient(HttpExchange exchange) throws IOException {
            File file = new File("stats_query.html");
            if (!file.exists()) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
