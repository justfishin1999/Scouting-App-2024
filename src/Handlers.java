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
            	Utils.logMessage(e.getMessage());
                return "";
            }
        }
    	
    }
    static class CSSHandler implements HttpHandler {
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
            String response = readFile("style.css");
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
            } else if ("refreshData".equals(action)){
            	refreshData();
            	sendResponse(exchange, 200, "OK", "Data refreshsed successfully!");
            } else {
                sendResponse(exchange, 400, "Bad Request", "Invalid action parameter");
            }
        }

        private void resetData() {
            try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
                 Statement stmt = conn.createStatement()) {

                // Truncate the match_data table
                stmt.executeUpdate("TRUNCATE TABLE match_data");

                // Truncate the match_avg table
                stmt.executeUpdate("TRUNCATE TABLE match_avg");
                
                // Truncate the robot_data table
                stmt.executeUpdate("TRUNCATE TABLE robot_info");

                Utils.logMessage("Data reset successfully!");

            } catch (SQLException e) {
            	Utils.logMessage(e.getMessage());
            	Utils.logMessage("Failed to reset data");
            }
        }

        private static void backupData() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String backupFileName = "C:\\web\\backups\\backup_" + dateFormat.format(new Date(System.currentTimeMillis())) + ".bak";

            try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);
                 Statement stmt = conn.createStatement()) {

                // Execute the backup command
                String backupCommand = "BACKUP DATABASE Scout2024 TO DISK = '" + backupFileName + "'";
                stmt.execute(backupCommand);

                Utils.logMessage("Database backed up to: " + backupFileName);

            } catch (SQLException e) {
            	Utils.logMessage(e.getMessage());
            	Utils.logMessage("Failed to backup data");
            }
        }
        
        private static void refreshData() {
        	MatchData.publishMatchData();
        	AverageData.publishTeamAverages();
        	Utils.logMessage("Refreshed match and average data");
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
}