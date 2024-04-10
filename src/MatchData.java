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
                Utils.logMessage("405 - Method Not ALlowed - Unsupported HTTP Method");
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
                Utils.logMessage(e.getMessage());
                return "";
            }
        }
    	
    }
	public static void publishMatchData() {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Match Data</title>");
        htmlContent.append("<link rel=\"stylesheet\" href=\"style.css\">");
        htmlContent.append("</head>");
        htmlContent.append("<body><div class='navbar'>");
        htmlContent.append("<a href='/'>Home</a>");
        htmlContent.append("<a href='/pit-scout.html'>Pits</a>");
        htmlContent.append("<a href='/team_averages.html'>Team Averages</a>");
        htmlContent.append("<a class='active' href='/actual_stats.html'>Team Data</a>");
        htmlContent.append("<a href='/reports.html'>Reports</a>");
        htmlContent.append("<a href='/teams.html'>Teams</a>");
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
                htmlContent.append("<td>").append(autoMobility == 1 ? "Yes" : "No").append("</td><td>").append(notesTeleopSpeaker).append("</td>");
                htmlContent.append("<td>").append(notesTeleopAmp).append("</td><td>").append(cycleTimeTeleop).append("</td>");
                htmlContent.append("<td>").append(climbCompleted == 1 ? "Yes" : "No").append("</td><td>").append(noteTrap).append("</td></tr>");
            }
        } catch (SQLException e) {
        	Utils.logMessage(e.getMessage());
        }

        htmlContent.append("</table></div>");
        htmlContent.append("<div class='container'>");
        htmlContent.append("<h1>Robot Data</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Team Number</th><th>Ground Pickup</th><th>Podium Shot</th>");
        htmlContent.append("<th>Swerve</th><th>Speaker</th><th>Amp</th>");
        htmlContent.append("<th>Trap</th><th>Climb</th><th>Robot Speed</th></tr>");

        try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM robot_info");
            while (rs.next()) {
                int teamNumber = rs.getInt("team_number");
                int groundPickup = rs.getInt("ground_pickup");
                int shootFromPodium = rs.getInt("shoot_from_podium");
                int isSwerve = rs.getInt("is_swerve");
                int canShootSpeaker = rs.getInt("can_shoot_speaker");
                int canShootAmp = rs.getInt("can_shoot_amp");
                int canShootTrap = rs.getInt("can_shoot_trap");
                int canClimb = rs.getInt("can_climb");
                int estRobotSpeed = rs.getInt("est_robot_speed");

                htmlContent.append("<tr><td>").append(teamNumber).append("</td><td>").append(groundPickup == 1 ? "Yes" : "No").append("</td>");
                htmlContent.append("<td>").append(shootFromPodium == 1 ? "Yes" : "No").append("</td><td>").append(isSwerve == 1 ? "Yes" : "No").append("</td>");
                htmlContent.append("<td>").append(canShootSpeaker == 1 ? "Yes" : "No").append("</td><td>").append(canShootAmp == 1 ? "Yes" : "No").append("</td>");
                htmlContent.append("<td>").append(canShootTrap == 1 ? "Yes" : "No").append("</td><td>").append(canClimb == 1 ? "Yes" : "No").append("</td>");
                htmlContent.append("<td>").append(estRobotSpeed+" m/s").append("</td></tr>");
            }
        } catch (SQLException e) {
            Utils.logMessage(e.getMessage());
        }

        htmlContent.append("</table></div>");

        htmlContent.append("<script src='script-no-pwd.js'></script>");
        htmlContent.append("</body></html>");

        // Write HTML content to actual_stats.html file
        try {
            String filePath = "C:\\web\\actual_stats.html";
            Files.writeString(Paths.get(filePath), htmlContent.toString());
            Utils.logMessage("Match data published to actual_stats.html");
        } catch (IOException e) {
        	Utils.logMessage(e.getMessage());
        }
    }
}