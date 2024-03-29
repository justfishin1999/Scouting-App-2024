import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PitScout {
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
            String response = readFile("C:\\web\\pit-scout.html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Process form data as before
            String query = new String(exchange.getRequestBody().readAllBytes());
            String[] params = query.split("&");
            int teamNumber = Utils.parseOrDefault(params[0].split("=")[1], 0);
            int groundPickup = Utils.parseOrDefault(params[1].split("=")[1], 0);
            int shootFromPodium = Utils.parseOrDefault(params[2].split("=")[1], 0);
            int isSwerve = Utils.parseOrDefault(params[3].split("=")[1], 0);
            int canShootSpeaker = Utils.parseOrDefault(params[4].split("=")[1], 0);
            int canShootAmp = Utils.parseOrDefault(params[5].split("=")[1], 0);
            int canShootTrap = Utils.parseOrDefault(params[6].split("=")[1], 0);
            int canClimb = Utils.parseOrDefault(params[7].split("=")[1], 0);
            int estRobotSpeed = Utils.parseOrDefault(params[8].split("=")[1], 0);

            // Store data in the database
            storeData(teamNumber, groundPickup, shootFromPodium, isSwerve, canShootSpeaker,
                    canShootAmp, canShootTrap, canClimb, estRobotSpeed);

            // Respond to the client
            String response = "<html><body><p>Data submitted successfully!</p><a href=\"pit-scout.html\">Back to entry</a></body></html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void storeData(int teamNumber, int groundPickup, int shootFromPodium,
                               int isSwerve, int canShootSpeaker, int canShootAmp,
                               int canShootTrap, int canClimb, int estRobotSpeed) {
            // Store data in the database
            try (Connection conn = DriverManager.getConnection(Constants.JDBCConstants.url,
                    Constants.JDBCConstants.username, Constants.JDBCConstants.password);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO robot_info (" +
                                 "    team_number, " +
                                 "    ground_pickup, " +
                                 "    shoot_from_podium, " +
                                 "    is_swerve, " +
                                 "    can_shoot_speaker, " +
                                 "    can_shoot_amp, " +
                                 "    can_shoot_trap, " +
                                 "    can_climb, " +
                                 "    est_robot_speed" +
                                 ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                 )
            ) {
                stmt.setInt(1, teamNumber);
                stmt.setInt(2, groundPickup);
                stmt.setInt(3, shootFromPodium);
                stmt.setInt(4, isSwerve);
                stmt.setInt(5, canShootSpeaker);
                stmt.setInt(6, canShootAmp);
                stmt.setInt(7, canShootTrap);
                stmt.setInt(8, canClimb);
                stmt.setInt(9, estRobotSpeed);
                stmt.executeUpdate();
                
                MatchData.publishMatchData();
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
