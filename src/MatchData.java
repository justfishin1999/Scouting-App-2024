import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MatchData {
    static class statsHandler implements HttpHandler {
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
            // Read data_management.html and serve its contents
            String response = readFile("actual_stats.html");
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
	public static void publishMatchData() {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Match Data</title>");
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
        htmlContent.append("<a href='/actual_stats.html'>Actual Stats By Team</a>");
        //htmlContent.append("<a href='/teams.html'>Teams at Event</a>");
        htmlContent.append("<a href='https://thebluealliance.com'>The Blue Alliance</a>");
        htmlContent.append("<a href='/admin.html'>Admin</a>");
        htmlContent.append("<div class='clock' id='clock'></div>");
        htmlContent.append("</div><div class='container'>");
        htmlContent.append("<h1>Match Data</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Match Number</th><th>Team Number</th><th>Notes Auto Speaker</th>");
        htmlContent.append("<th>Notes Auto Amp</th><th>Auto Mobility</th><th>Notes Teleop Speaker</th>");
        htmlContent.append("<th>Notes Teleop Amp</th><th>Defensive Ranking</th><th>Climb Completed</th>");
        htmlContent.append("<th>Note Trap</th></tr>");

        try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM match_data");
            while (rs.next()) {
                int matchNumber = rs.getInt("match_number");
                int teamNumber = rs.getInt("team_number");
                double notesAutoSpeaker = rs.getDouble("notes_auto_speaker");
                double notesAutoAmp = rs.getDouble("notes_auto_amp");
                double autoMobility = rs.getDouble("auto_mobility");
                double notesTeleopSpeaker = rs.getDouble("notes_teleop_speaker");
                double notesTeleopAmp = rs.getDouble("notes_teleop_amp");
                double cycleTimeTeleop = rs.getDouble("cycle_time_teleop");
                double climbCompleted = rs.getDouble("climb_completed");
                double noteTrap = rs.getDouble("note_trap");

                htmlContent.append("<tr><td>").append(matchNumber).append("</td><td>").append(teamNumber).append("</td>");
                htmlContent.append("<td>").append(notesAutoSpeaker).append("</td><td>").append(notesAutoAmp).append("</td>");
                htmlContent.append("<td>").append(autoMobility).append("</td><td>").append(notesTeleopSpeaker).append("</td>");
                htmlContent.append("<td>").append(notesTeleopAmp).append("</td><td>").append(cycleTimeTeleop).append("</td>");
                htmlContent.append("<td>").append(climbCompleted).append("</td><td>").append(noteTrap).append("</td></tr>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlContent.append("</table></div>");
        htmlContent.append("<script src='script-no-pwd.js'></script>");
        htmlContent.append("<center><p>FRC Scouting App - V0.1.3<br>Developed by Justin F (FRC 4728) - 2024</p></center>\r\n"
                + "</body></html>");

        // Write HTML content to actual_stats.html file
        try {
            String filePath = "C:\\web\\actual_stats.html";
            Files.writeString(Paths.get(filePath), htmlContent.toString());
            System.out.println("Match data published to actual_stats.html");
            System.out.println("---------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}