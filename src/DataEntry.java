import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DataEntry{
    static class DataEntryHandler implements HttpHandler {
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
            int matchNumber = Utils.parseOrDefault(params[0].split("=")[1], 0);
            int teamNumber = Utils.parseOrDefault(params[1].split("=")[1], 0);
            int notesAutoSpeaker = Utils.parseOrDefault(params[2].split("=")[1], 0);
            int notesAutoAmp = Utils.parseOrDefault(params[3].split("=")[1], 0);
            int autoMobility = Utils.parseOrDefault(params[4].split("=")[1], 0);
            int notesTeleopSpeaker = Utils.parseOrDefault(params[5].split("=")[1], 0);
            int notesTeleopAmp = Utils.parseOrDefault(params[6].split("=")[1], 0);
            int defenseRanking = Utils.parseOrDefault(params[7].split("=")[1], 0);
            int climbCompleted = Utils.parseOrDefault(params[8].split("=")[1], 0);
            int noteTrap = Utils.parseOrDefault(params[9].split("=")[1], 0);

            // Store data in the database
            storeData(matchNumber, teamNumber, notesAutoSpeaker, notesAutoAmp, autoMobility,
                    notesTeleopSpeaker, notesTeleopAmp, defenseRanking, climbCompleted, noteTrap);

            // Respond to the client with a JavaScript response
            String jsResponse = "<script>alert('Data submitted successfully!'); window.location.href = 'index.html';</script>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, jsResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsResponse.getBytes());
            os.close();
        }

        class Response {
            private String message;

            public Response(String message) {
                this.message = message;
            }

            // Getter and setter for the message field
            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }
        

        
        private void storeData(int matchNumber, int teamNumber, int notesAutoSpeaker,
                               int notesAutoAmp, int autoMobility, int notesTeleopSpeaker,
                               int notesTeleopAmp, int defenseRanking, int climbCompleted,
                               int noteTrap) {
            // Store data in the database as before
            try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.USERNAME, Constants.JDBCConstants.PASSWORD);
            	     PreparedStatement stmt = conn.prepareStatement(
            	         "INSERT INTO match_data (" +
            	         "    match_number, " +
            	         "    team_number, " +
            	         "    notes_auto_speaker, " +
            	         "    notes_auto_amp, " +
            	         "    auto_mobility, " +
            	         "    notes_teleop_speaker, " +
            	         "    notes_teleop_amp, " +
            	         "    cycle_time_teleop, " +
            	         "    climb_completed, " +
            	         "    note_trap" +
            	         ") VALUES (?, ?, COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0), COALESCE(?, 0))"
            	     )
            	){
                stmt.setInt(1, matchNumber);
                stmt.setInt(2, teamNumber);
                stmt.setInt(3, notesAutoSpeaker);
                stmt.setInt(4, notesAutoAmp);
                stmt.setInt(5, autoMobility);
                stmt.setInt(6, notesTeleopSpeaker);
                stmt.setInt(7, notesTeleopAmp);
                stmt.setInt(8, defenseRanking);
                stmt.setInt(9, climbCompleted);
                stmt.setInt(10, noteTrap);
                stmt.executeUpdate();
                AverageData.calculateAndStoreAverages();
                MatchData.publishMatchData();
                
            } catch (SQLException e) {
                Utils.logMessage(e.getMessage());
            }
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