import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Handlers {
	static class javascriptHandler implements HttpHandler {
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
            String response = readFile("script.js");
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
    static class javascriptHandler2 implements HttpHandler {
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
            String response = readFile("script-no-pwd.js");
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

    static class AccessDenied implements HttpHandler {
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
            String response = readFile("access_denied.html");
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
    static class DataManagementHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if ("GET".equals(requestMethod)) {
                handleGetRequest(exchange);
            } else if ("POST".equals(requestMethod)) {
                handlePostRequest(exchange);
            } else {
                // Unsupported HTTP method
                sendResponse(exchange, 405, "Method Not Allowed", "Unsupported HTTP method");
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // Read data_management.html and serve its contents
            String response = readFile("data_management.html");
            sendResponse(exchange, 200, "OK", response);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Get the action parameter from the request body
            String query = new String(exchange.getRequestBody().readAllBytes());
            String[] params = query.split("&");
            String action = params[0].split("=")[1];

            if ("reset".equals(action)) {
                resetData();
                sendResponse(exchange, 200, "OK", "Data reset successfully!");
            } else if ("backup".equals(action)) {
                backupData();
                sendResponse(exchange, 200, "OK", "Data backed up successfully!");
            } else {
                sendResponse(exchange, 400, "Bad Request", "Invalid action parameter");
            }
        }

        private void resetData() {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
            String username = "frc2024";
            String password = "9Ng83$#8jg83gjusdwe89";

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement stmt = conn.createStatement()) {

                // Truncate the match_data table
                stmt.executeUpdate("TRUNCATE TABLE match_data");

                // Truncate the match_avg table
                stmt.executeUpdate("TRUNCATE TABLE match_avg");

                System.out.println("Data reset successfully!");
                System.out.println("---------------------------------");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private static void backupData() {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
            String username = "frc2024";
            String password = "9Ng83$#8jg83gjusdwe89";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String backupFileName = "C:\\web\\backups\\backup_" + dateFormat.format(new Date(System.currentTimeMillis())) + ".bak";

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement stmt = conn.createStatement()) {

                // Execute the backup command
                String backupCommand = "BACKUP DATABASE Scout2024 TO DISK = '" + backupFileName + "'";
                stmt.execute(backupCommand);

                System.out.println("Database backed up to: " + backupFileName);
                System.out.println("---------------------------------");

            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            int matchNumber = parseOrDefault(params[0].split("=")[1], 0);
            int teamNumber = parseOrDefault(params[1].split("=")[1], 0);
            int notesAutoSpeaker = parseOrDefault(params[2].split("=")[1], 0);
            int notesAutoAmp = parseOrDefault(params[3].split("=")[1], 0);
            int autoMobility = parseOrDefault(params[4].split("=")[1], 0);
            int notesTeleopSpeaker = parseOrDefault(params[5].split("=")[1], 0);
            int notesTeleopAmp = parseOrDefault(params[6].split("=")[1], 0);
            int defenseRanking = parseOrDefault(params[7].split("=")[1], 0);
            int climbCompleted = parseOrDefault(params[8].split("=")[1], 0);
            int noteTrap = parseOrDefault(params[9].split("=")[1], 0);

            // Store data in the database
            storeData(matchNumber, teamNumber, notesAutoSpeaker, notesAutoAmp, autoMobility,
                    notesTeleopSpeaker, notesTeleopAmp, defenseRanking, climbCompleted, noteTrap);

            // Respond to the client
            String response = "<html><body><p>Data submitted successfully!</p><a href=\"index.html\">Back to entry</a></body></html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        private int parseOrDefault(String value, int defaultValue) {
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        }
        

        private void storeData(int matchNumber, int teamNumber, int notesAutoSpeaker,
                               int notesAutoAmp, int autoMobility, int notesTeleopSpeaker,
                               int notesTeleopAmp, int defenseRanking, int climbCompleted,
                               int noteTrap) {
            // Store data in the database as before
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
            String username = "frc2024";
            String password = "9Ng83$#8jg83gjusdwe89";

            try (Connection conn = DriverManager.getConnection(url, username, password);
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
                App.calculateAndStoreAverages();
                App.publishMatchData();
                
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
}