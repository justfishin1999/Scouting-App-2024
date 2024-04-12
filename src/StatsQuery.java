import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class StatsQuery {
    public static class TeamsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String eventCode = Constants.TBA_API.TBA_EVENT;
                String teamMatchData = fetchTeamMatchData(eventCode);
                String htmlContent = generateHtmlPage(teamMatchData);
                sendResponse(exchange, htmlContent);
            } else {
                // Unsupported HTTP method
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        public static String fetchTeamMatchData(String eventCode) throws IOException {
            StringBuilder teamMatchData = new StringBuilder();
            String apiUrl = "https://api.statbotics.io/v2/team_matches/event/" + eventCode;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNextLine()) {
                    teamMatchData.append(scanner.nextLine());
                }
                scanner.close();
            } else {
                System.err.println("Failed to retrieve data. Response code: " + responseCode);
            }
            connection.disconnect();
            return teamMatchData.toString();
        }

        public static String generateHtmlPage(String teamMatchData) {
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><head><title>Team Matches</title>");
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
            htmlContent.append("</div>");
            htmlContent.append("<div class='container'>");
            htmlContent.append("<h1>Team Matches</h1>");
            htmlContent.append("<table border='1'>");
            htmlContent.append("<tr>");
            htmlContent.append("<th>Match</th>");
            htmlContent.append("<th>Event</th>");
            htmlContent.append("<th>Time</th>");
            htmlContent.append("<th>Alliance</th>");
            htmlContent.append("<th>Status</th>");
            htmlContent.append("<th>EPA</th>");
            htmlContent.append("<th>Auto EPA</th>");
            htmlContent.append("<th>Teleop EPA</th>");
            htmlContent.append("<th>Endgame EPA</th>");
            htmlContent.append("<th>RP 1 EPA</th>");
            htmlContent.append("<th>RP 2 EPA</th>");
            htmlContent.append("<th>Post EPA</th>");
            htmlContent.append("</tr>");

            // Parse team match data and append rows to the table
            Gson gson = new Gson();
            JsonArray matchesArray = gson.fromJson(teamMatchData, JsonArray.class);
            for (JsonElement matchElement : matchesArray) {
                JsonObject matchObject = matchElement.getAsJsonObject();
                htmlContent.append("<tr>");
                htmlContent.append("<td>").append(getValueAsString(matchObject, "match")).append("</td>");
                htmlContent.append("<td>").append(getValueAsString(matchObject, "event")).append("</td>");
                htmlContent.append("<td>").append(getValueAsString(matchObject, "time")).append("</td>");
                htmlContent.append("<td>").append(getValueAsString(matchObject, "alliance")).append("</td>");
                htmlContent.append("<td>").append(getValueAsString(matchObject, "status")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "auto_epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "teleop_epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "endgame_epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "rp_1_epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "rp_2_epa")).append("</td>");
                htmlContent.append("<td>").append(getValueAsDouble(matchObject, "post_epa")).append("</td>");
                htmlContent.append("</tr>");
            }

            htmlContent.append("</table>");
            htmlContent.append("</div></body></html>");
            return htmlContent.toString();
        }

        public static String getValueAsString(JsonObject jsonObject, String key) {
            JsonElement element = jsonObject.get(key);
            return (element != null && !element.isJsonNull()) ? element.getAsString() : "";
        }

        public static double getValueAsDouble(JsonObject jsonObject, String key) {
            JsonElement element = jsonObject.get(key);
            return (element != null && !element.isJsonNull()) ? element.getAsDouble() : 0.0;
        }



        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class StatsQueryHttp implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().toString();
            if ("/team_matches".equals(requestURI)) {
                TeamsHandler teamsHandler = new TeamsHandler();
                teamsHandler.handle(exchange);
            } else if ("/stats_query.html".equals(requestURI)) {
                serveHtmlPage(exchange);
            } else {
                // Unsupported URI
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }
        }

        private void serveHtmlPage(HttpExchange exchange) throws IOException {
            // Fetch team match data
            String eventCode = Constants.TBA_API.TBA_EVENT;
            String teamMatchData = TeamsHandler.fetchTeamMatchData(eventCode);
            
            // Generate HTML content
            String htmlContent = TeamsHandler.generateHtmlPage(teamMatchData);

            // Write HTML content to file
            try (FileWriter writer = new FileWriter("stats_query.html")) {
                writer.write(htmlContent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Serve the HTML content
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, htmlContent.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(htmlContent.getBytes());
            os.close();
        }

    }
}
