import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeamList {
    public static class TeamsHandler implements HttpHandler {
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

        public static String fetchTeamData() {
            StringBuilder teamData = new StringBuilder();
            try {
                // Blue Alliance API key
                String apiKey = Constants.PasswordConstants.APIKEY;
                // Event code for the Granite City Regional
                String eventCode = Constants.TBA_API.TBA_EVENT;

                // Create URL object
                @SuppressWarnings("deprecation")
				URL url = new URL("https://www.thebluealliance.com/api/v3/event/" + eventCode + "/teams");

                // Open connection
                Utils.logMessage("Attempting API connection to: "+url);
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
                    Utils.logMessage("Got data from TBA using API key: "+eventCode+"");
                } else {
                    Utils.logMessage("Failed to retrieve data. Response code: " + responseCode);
                }

                // Close connection
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return teamData.toString();
        }

        public static void writeTeamsHtml(String teamData) {
            try (PrintWriter htmlContent = new PrintWriter(new FileWriter("teams.html"))) {
            	htmlContent.append("<html><head><title>Team Data</title>");
                htmlContent.append("<style>");
                htmlContent.append("body {font-family: Arial, sans-serif; background-color: #f0f0f0; margin: 0; padding: 0;}");
                htmlContent.append(".navbar {overflow: hidden; background-color: #333;}");
                htmlContent.append(".navbar a {float: left; display: block; color: #f2f2f2; text-align: center; padding: 14px 20px; text-decoration: none;}");
                htmlContent.append(".navbar a:hover {background-color: #ddd; color: black;}");
                htmlContent.append(".navbar .clock {float: right; color: #f2f2f2; padding: 14px 20px;}");
                htmlContent.append(".container {width: 800px; margin: 20px auto; padding: 20px; background-color: #fff; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);}");
                htmlContent.append("h1 {text-align: center; margin-bottom: 20px;}");
                htmlContent.append("table {width: 100%; border-collapse: collapse;}");
                htmlContent.append("th, td {padding: 8px; text-align: left; border-bottom: 1px solid #ddd;}");
                htmlContent.append("tr:nth-child(even) {background-color: #f2f2f2;}");
                htmlContent.append("</style></head>");
                htmlContent.append("<body><div class='navbar'>");
                htmlContent.append("<a href='/'>Home</a>");
                htmlContent.append("<a href='/pit-scout.html'>Pits</a>");
                htmlContent.append("<a href='/team_averages.html'>Team Averages</a>");
                htmlContent.append("<a href='/actual_stats.html'>Team Data</a>");
                htmlContent.append("<a href='/reports.html'>Reports</a>");
                htmlContent.append("<a href='/teams.html'>Teams</a>");
                htmlContent.append("<a href='/admin.html'>Admin</a>");
                htmlContent.append("<div class='clock' id='clock'></div>");
                htmlContent.append("</div><div class='container'>");
                htmlContent.append("<h1>Team List</h1>");
                htmlContent.append("<center><p>Showing info from <i><a href='https://thebluealliance.com'>The Blue Alliance</a></i> for event ID: "+ Constants.TBA_API.TBA_EVENT +"</p></center>");
                htmlContent.append("<table>");
                htmlContent.append("<tr><th>Team Number</th><th>Team Name</th><th>Location</th><th>Country</th></tr>");

                // Parse JSON response
                Gson gson = new Gson();
                JsonArray teamsArray = JsonParser.parseString(teamData).getAsJsonArray();
                for (JsonElement teamElement : teamsArray) {
                    String teamNumber = teamElement.getAsJsonObject().get("team_number").getAsString();
                    String teamName = teamElement.getAsJsonObject().get("nickname").getAsString();
                    String location = teamElement.getAsJsonObject().get("city").getAsString() + ", " +
                                      teamElement.getAsJsonObject().get("state_prov").getAsString();
                    String country = teamElement.getAsJsonObject().get("country").getAsString();

                    htmlContent.append("<tr><td>" + teamNumber + "</td><td>" + teamName + "</td><td>" + location + "</td><td>" + country + "</td></tr>");
                }

                htmlContent.append("</table></div>");
                htmlContent.append("<script src='script-no-pwd.js'></script>");
                htmlContent.append("</body></html>");
            } catch (IOException e) {
                Utils.logMessage(e.getMessage());
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
    	public static void updateAPIData() {
    		// TODO Auto-generated method stub
    		writeTeamsHtml(fetchTeamData());
    	}
        
    }
}
