import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamList {
    static class TeamsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Call the API to fetch team data
                String teamData = fetchTeamData();

                // Write the team data to teams.html
                writeTeamsHtml(teamData);

                // Send teams.html to the client
                sendTeamsHtmlToClient(exchange);
            } else {
                // Unsupported HTTP method
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }

        private String fetchTeamData() {
            StringBuilder teamData = new StringBuilder();
            try {
                // Blue Alliance API key
                String apiKey = "BBajf45PGya7yhQ8pvRebwYJeMPg6vWGUJbo7u5oWMGavWlXVrJ2iFMgqy0ExwUd";
                // Event code for the Granite City Regional
                String eventCode = "2024mnmi2";

                // Create URL object
                URL url = new URL("https://www.thebluealliance.com/api/v3/event/" + eventCode + "/teams");

                // Open connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set request properties
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-TBA-Auth-Key", apiKey);
                connection.setRequestProperty("User-Agent", "Java-App-1.0");

                // Get response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        teamData.append(line);
                    }
                    reader.close();
                } else {
                    System.out.println("Failed to retrieve data. Response code: " + responseCode);
                }

                // Close connection
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return teamData.toString();
        }

        private void writeTeamsHtml(String teamData) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("teams.html"))) {
                writer.append("<html><head><title>Match Data</title>");
                writer.append("<style>");
                writer.append("body {font-family: Arial, sans-serif; background-color: #f0f0f0; margin: 0; padding: 0;}");
                writer.append(".navbar {overflow: hidden; background-color: #333;}");
                writer.append(".navbar a {float: left; display: block; color: #f2f2f2; text-align: center; padding: 14px 20px; text-decoration: none;}");
                writer.append(".navbar a:hover {background-color: #ddd; color: black;}");
                writer.append(".navbar .clock {float: right; color: #f2f2f2; padding: 14px 20px;}");
                writer.append(".container {width: 800px; margin: 20px auto; padding: 20px; background-color: #fff; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);}");
                writer.append("h1 {text-align: center; margin-bottom: 20px;}");
                writer.append("table {width: 100%; border-collapse: collapse;}");
                writer.append("th, td {padding: 8px; text-align: left; border-bottom: 1px solid #ddd;}");
                writer.append("tr:nth-child(even) {background-color: #f2f2f2;}");
                writer.append("</style></head>");
                writer.append("<body><div class='navbar'>");
                writer.append("<a href='/'>Home</a>");
                writer.append("<a href='/team_averages.html'>Team Averages</a>");
                writer.append("<a href='/actual_stats.html'>Actual Stats By Team</a>");
                writer.append("<a href='/teams.html'>Teams at Event</a>");
                writer.append("<a href='https://thebluealliance.com'>The Blue Alliance</a>");
                writer.append("<a href='/admin.html'>Admin</a>");
                writer.append("<div class='clock' id='clock'></div>");
                writer.append("<div class=\"container\">");
                writer.append("<h1>Team List</h1>");
                // Start table
                writer.append("<table>");
                writer.append("<tr><th>Team Number</th><th>Team Name</th><th>Location</th><th>Country</th></tr>");

                // Parse team data JSON and add rows to table
                Pattern pattern = Pattern.compile("\\{\"team_number\":\"(.*?)\",\"nickname\":\"(.*?)\",\"city\":\"(.*?)\",\"state_prov\":\"(.*?)\",\"country\":\"(.*?)\"\\}");
                Matcher matcher = pattern.matcher(teamData);
                while (matcher.find()) {
                    String teamNumber = matcher.group(1);
                    String teamName = matcher.group(2);
                    String location = matcher.group(3) + ", " + matcher.group(4);
                    String country = matcher.group(5);

                    writer.append("<tr><td>" + teamNumber + "</td><td>" + teamName + "</td><td>" + location + "</td><td>" + country + "</td></tr>");
                }

                // End table
                writer.append("</table>");
                writer.append("</div>");
                writer.append("</body>");
                writer.append("</html>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendTeamsHtmlToClient(HttpExchange exchange) throws IOException {
            File file = new File("teams.html");
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
