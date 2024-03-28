import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AverageData {
	static class TeamAveragesHandler implements HttpHandler {
        @Override
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
            // Read team_averages.html and serve its contents
            String response = readFile("team_averages.html");
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
                e.printStackTrace();
                return "";
            }
        }
    }
    public static void calculateAndStoreAverages() {
        try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
             Statement stmt = conn.createStatement()) {

            // List of columns to calculate averages for
            String[] columns = {"notes_auto_speaker", "notes_auto_amp", "auto_mobility",
                                "notes_teleop_speaker", "notes_teleop_amp", "cycle_time_teleop",
                                "climb_completed", "note_trap"};

            // Iterate through each column to calculate averages for each team
            for (String column : columns) {
                ResultSet rs = stmt.executeQuery("SELECT team_number, AVG(" + column + ") AS average_" + column +
                                                  " FROM match_data GROUP BY team_number");

                // Store or update averages in match_avg table
                PreparedStatement mergeStmt = conn.prepareStatement(
                        "MERGE INTO match_avg AS target" +
                        " USING (VALUES (?, ?)) AS source (team_number, average)" +
                        " ON target.team_number = source.team_number" +
                        " WHEN MATCHED THEN" +
                        " UPDATE SET target.average_" + column + " = source.average" +
                        " WHEN NOT MATCHED THEN" +
                        " INSERT (team_number, average_" + column + ") VALUES (source.team_number, source.average);");

                while (rs.next()) {
                    int teamNumber = rs.getInt("team_number");
                    double averageScore = rs.getDouble("average_" + column);
                    mergeStmt.setInt(1, teamNumber);
                    mergeStmt.setDouble(2, averageScore);
                    mergeStmt.executeUpdate();
                }
            }
            publishTeamAverages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void publishTeamAverages() {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Team Averages</title>");
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
        htmlContent.append("<a href='/team_averages.html'>Team Averages</a>");
        htmlContent.append("<a href='/actual_stats.html'>Team Data</a>");
        htmlContent.append("<a href='/teams.html'>Teams</a>");
        htmlContent.append("<a href='https://thebluealliance.com')>The Blue Alliance</a>");
        htmlContent.append("<a href='/admin.html'>Admin</a>");
        htmlContent.append("<div class='clock' id='clock'></div>");
        htmlContent.append("</div><div class='container'>");
        htmlContent.append("<h1>Team Averages</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Team Number</th><th>Notes Auto Speaker</th><th>Notes Auto Amp</th>");
        htmlContent.append("<th>Auto Mobility</th><th>Notes Teleop Speaker</th><th>Notes Teleop Amp</th>");
        htmlContent.append("<th>Defensive Ranking</th><th>Climb Completed</th><th>Notes Trap</th></tr>");

        try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM match_avg");
            while (rs.next()) {
                int teamNumber = rs.getInt("team_number");
                double notesAutoSpeaker = rs.getDouble("average_notes_auto_speaker");
                double notesAutoAmp = rs.getDouble("average_notes_auto_amp");
                double autoMobility = rs.getDouble("average_auto_mobility");
                double notesTeleopSpeaker = rs.getDouble("average_notes_teleop_speaker");
                double notesTeleopAmp = rs.getDouble("average_notes_teleop_amp");
                double defenseRanking = rs.getDouble("average_cycle_time_teleop");
                double climbCompleted = rs.getDouble("average_climb_completed");
                double noteTrap = rs.getDouble("average_note_trap");

                htmlContent.append("<tr><td>").append(teamNumber).append("</td><td>").append(notesAutoSpeaker).append("</td>");
                htmlContent.append("<td>").append(notesAutoAmp).append("</td><td>").append(autoMobility).append("</td>");
                htmlContent.append("<td>").append(notesTeleopSpeaker).append("</td><td>").append(notesTeleopAmp).append("</td>");
                htmlContent.append("<td>").append(defenseRanking).append("</td><td>").append(climbCompleted).append("</td>");
                htmlContent.append("<td>").append(noteTrap).append("</td></tr>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlContent.append("</table></div>");
        htmlContent.append("<script src='script-no-pwd.js'></script>");
        htmlContent.append("<center><p>FRC Scouting App - V0.1.5<br>Developed by Justin F (FRC 4728) - 2024</p></center>\r\n"
        		+ "</body></html>");

        // Write HTML content to team_averages.html file
        try {
            String filePath = "C:\\web\\team_averages.html";
            Files.writeString(Paths.get(filePath), htmlContent.toString());
            System.out.println("Team averages published to team_averages.html");
            System.out.println("---------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}