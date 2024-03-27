import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.*;

public class App {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new Handlers.MyHandler());
        server.createContext("/team_averages.html", new Handlers.TeamAveragesHandler());
        server.createContext("/data_management.html", new Handlers.DataManagementHandler());
        server.createContext("/access_denied.html", new Handlers.AccessDenied());
        server.createContext("/script.js", new Handlers.javascriptHandler());
        server.createContext("/actual_stats.html", new Handlers.statsHandler());
        server.createContext("/script-no-pwd.js", new Handlers.javascriptHandler2());
        server.start();
        System.out.println("**********************************");
        System.out.println("Starting FRC Scouting App");
        System.out.println("**********************************");
        System.out.println("Server is running on port 8000...");
        System.out.println("Server version v0.0.9 - beta");
        System.out.println("**********************************");

        // Calculate and store averages
        calculateAndStoreAverages();
        publishMatchData();
        
    }

    public static void calculateAndStoreAverages() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
        String username = "frc2024";
        String password = "9Ng83$#8jg83gjusdwe89";

        try (Connection conn = DriverManager.getConnection(url, username, password);
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
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
        String username = "frc2024";
        String password = "9Ng83$#8jg83gjusdwe89";

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
        htmlContent.append("<a href='/actual_stats.html'>Actual Stats By Team</a>");
        htmlContent.append("<a href='https://thebluealliance.com')>The Blue Alliance</a>");
        htmlContent.append("<div class='clock' id='clock'></div>");
        htmlContent.append("</div><div class='container'>");
        htmlContent.append("<h1>Team Averages</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Team Number</th><th>Notes Auto Speaker</th><th>Notes Auto Amp</th>");
        htmlContent.append("<th>Auto Mobility</th><th>Notes Teleop Speaker</th><th>Notes Teleop Amp</th>");
        htmlContent.append("<th>Defensive Ranking</th><th>Climb Completed</th><th>Notes Trap</th></tr>");

        try (Connection conn = DriverManager.getConnection(url, username, password);
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
        htmlContent.append("<center><p>FRC Scouting App - V0.0.9<br>Developed by Justin F (FRC 4728) - 2024</p></center>\r\n"
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
    public static void publishMatchData() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
        String username = "frc2024";
        String password = "9Ng83$#8jg83gjusdwe89";

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
        htmlContent.append("<a href='https://thebluealliance.com'>The Blue Alliance</a>");
        htmlContent.append("<div class='clock' id='clock'></div>");
        htmlContent.append("</div><div class='container'>");
        htmlContent.append("<h1>Match Data</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Match Number</th><th>Team Number</th><th>Notes Auto Speaker</th>");
        htmlContent.append("<th>Notes Auto Amp</th><th>Auto Mobility</th><th>Notes Teleop Speaker</th>");
        htmlContent.append("<th>Notes Teleop Amp</th><th>Defensive Ranking</th><th>Climb Completed</th>");
        htmlContent.append("<th>Note Trap</th></tr>");

        try (Connection conn = DriverManager.getConnection(url, username, password);
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
        htmlContent.append("<center><p>FRC Scouting App - V0.0.9<br>Developed by Justin F (FRC 4728) - 2024</p></center>\r\n"
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
