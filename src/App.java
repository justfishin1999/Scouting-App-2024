import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.*;

import com.sun.net.httpserver.*;

public class App {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.createContext("/team_averages.html", new TeamAveragesHandler());
        server.start();
        System.out.println("Server is running on port 8000...");
        System.out.println("Server version v0.0.4 - alpha");

        // Calculate and store averages
        calculateAndStoreAverages();
    }
    
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

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // Read index.html and serve its contents
            String response = readFile("C:\\web\\index.html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Process form data as before
            String query = new String(exchange.getRequestBody().readAllBytes());
            String[] params = query.split("&");
            int matchNumber = Integer.parseInt(params[0].split("=")[1]);
            int teamNumber = Integer.parseInt(params[1].split("=")[1]);
            int notesAutoSpeaker = Integer.parseInt(params[2].split("=")[1]);
            int notesAutoAmp = Integer.parseInt(params[3].split("=")[1]);
            boolean autoMobility = params[4].split("=").length > 1;
            int notesTeleopSpeaker = Integer.parseInt(params[5].split("=")[1]);
            int notesTeleopAmp = Integer.parseInt(params[6].split("=")[1]);
            int cycleTimeTeleop = Integer.parseInt(params[7].split("=")[1]);
            boolean climbCompleted = params[8].split("=").length > 1;
            boolean noteTrap = params[9].split("=").length > 1;

            // Store data in the database
            storeData(matchNumber, teamNumber, notesAutoSpeaker, notesAutoAmp, autoMobility,
                    notesTeleopSpeaker, notesTeleopAmp, cycleTimeTeleop, climbCompleted, noteTrap);

            // Respond to the client
            String response = "<html><body><p>Data submitted successfully!</p><a href=\"index.html\">Back to entry</a></body></html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        

        private void storeData(int matchNumber, int teamNumber, int notesAutoSpeaker,
                               int notesAutoAmp, boolean autoMobility, int notesTeleopSpeaker,
                               int notesTeleopAmp, int cycleTimeTeleop, boolean climbCompleted,
                               boolean noteTrap) {
            // Store data in the database as before
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
            String username = "frc2024";
            String password = "9Ng83$#8jg83gjusdwe89";

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO match_data (match_number, team_number, notes_auto_speaker, " +
                                 "notes_auto_amp, auto_mobility, notes_teleop_speaker, notes_teleop_amp, " +
                                 "cycle_time_teleop, climb_completed, note_trap) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setInt(1, matchNumber);
                stmt.setInt(2, teamNumber);
                stmt.setInt(3, notesAutoSpeaker);
                stmt.setInt(4, notesAutoAmp);
                stmt.setBoolean(5, autoMobility);
                stmt.setInt(6, notesTeleopSpeaker);
                stmt.setInt(7, notesTeleopAmp);
                stmt.setInt(8, cycleTimeTeleop);
                stmt.setBoolean(9, climbCompleted);
                stmt.setBoolean(10, noteTrap);

                stmt.executeUpdate();
                calculateAndStoreAverages();
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    private static void calculateAndStoreAverages() {
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
    private static void publishTeamAverages() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
        String username = "frc2024";
        String password = "9Ng83$#8jg83gjusdwe89";

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Team Averages</title>");
        htmlContent.append("<style>");
        htmlContent.append("body {font-family: Arial, sans-serif; background-color: #f0f0f0;}");
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
        htmlContent.append("<div class='clock' id='clock'></div>");
        htmlContent.append("</div><div class='container'>");
        htmlContent.append("<h1>Team Averages</h1>");
        htmlContent.append("<table>");
        htmlContent.append("<tr><th>Team Number</th><th>Notes Auto Speaker</th><th>Notes Auto Amp</th>");
        htmlContent.append("<th>Auto Mobility</th><th>Notes Teleop Speaker</th><th>Notes Teleop Amp</th>");
        htmlContent.append("<th>Cycle Time Teleop</th><th>Climb Completed</th><th>Note Trap</th></tr>");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM match_avg");
            while (rs.next()) {
                int teamNumber = rs.getInt("team_number");
                int notesAutoSpeaker = rs.getInt("average_notes_auto_speaker");
                int notesAutoAmp = rs.getInt("average_notes_auto_amp");
                int autoMobility = rs.getInt("average_auto_mobility");
                int notesTeleopSpeaker = rs.getInt("average_notes_teleop_speaker");
                int notesTeleopAmp = rs.getInt("average_notes_teleop_amp");
                int cycleTimeTeleop = rs.getInt("average_cycle_time_teleop");
                int climbCompleted = rs.getInt("average_climb_completed");
                int noteTrap = rs.getInt("average_note_trap");

                htmlContent.append("<tr><td>").append(teamNumber).append("</td><td>").append(notesAutoSpeaker).append("</td>");
                htmlContent.append("<td>").append(notesAutoAmp).append("</td><td>").append(autoMobility).append("</td>");
                htmlContent.append("<td>").append(notesTeleopSpeaker).append("</td><td>").append(notesTeleopAmp).append("</td>");
                htmlContent.append("<td>").append(cycleTimeTeleop).append("</td><td>").append(climbCompleted).append("</td>");
                htmlContent.append("<td>").append(noteTrap).append("</td></tr>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlContent.append("</table></div><script>");
        htmlContent.append("function updateClock() {");
        htmlContent.append("var now = new Date();");
        htmlContent.append("var time = now.getHours() + ':' + (now.getMinutes() < 10 ? '0' : '') + now.getMinutes() + ':' + (now.getSeconds() < 10 ? '0' : '') + now.getSeconds();");
        htmlContent.append("document.getElementById('clock').textContent = 'Current Time: ' + time;");
        htmlContent.append("setTimeout(updateClock, 1000);");
        htmlContent.append("}");
        htmlContent.append("updateClock();");
        htmlContent.append("</script></body></html>");

        // Write HTML content to team_averages.html file
        try {
            String filePath = "C:\\web\\team_averages.html";
            Files.writeString(Paths.get(filePath), htmlContent.toString());
            System.out.println("Team averages published to team_averages.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
