import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

import com.sun.net.httpserver.*;

public class TeamInfoServlet {
    public static class TeamInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else {
                // Respond with 405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Extract teamNumber from request
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine(); // Assuming only one line of data
            String teamNumber = formData.split("=")[1];

            // Process request
            String response = processRequest(teamNumber);

            // Send response
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String processRequest(String teamNumber) {
            StringBuilder teamInfo = new StringBuilder();
            try {
                Connection con = DriverManager.getConnection(Constants.JDBCConstants.url, Constants.JDBCConstants.username, Constants.JDBCConstants.password);

                // Query match_data
                String queryMatchData = "SELECT * FROM match_data WHERE team_number = ?";
                PreparedStatement pstmtMatchData = con.prepareStatement(queryMatchData);
                pstmtMatchData.setString(1, teamNumber);
                ResultSet rsMatchData = pstmtMatchData.executeQuery();
                teamInfo.append("<h2>Match Data</h2>");
                teamInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">\r\n");
                teamInfo.append(generateTable(rsMatchData));

                // Query match_avg
                String queryMatchAvg = "SELECT * FROM match_avg WHERE team_number = ?";
                PreparedStatement pstmtMatchAvg = con.prepareStatement(queryMatchAvg);
                pstmtMatchAvg.setString(1, teamNumber);
                ResultSet rsMatchAvg = pstmtMatchAvg.executeQuery();
                teamInfo.append("<h2>Match Averages</h2>");
                teamInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">\r\n");
                teamInfo.append(generateTable(rsMatchAvg));

                // Query robot_info
                String queryRobotInfo = "SELECT * FROM robot_info WHERE team_number = ?";
                PreparedStatement pstmtRobotInfo = con.prepareStatement(queryRobotInfo);
                pstmtRobotInfo.setString(1, teamNumber);
                ResultSet rsRobotInfo = pstmtRobotInfo.executeQuery();
                teamInfo.append("<h2>Robot Info</h2>");
                teamInfo.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">\r\n");
                teamInfo.append(generateTable(rsRobotInfo));

                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return teamInfo.toString();
        }

        private String generateTable(ResultSet resultSet) throws SQLException {
            // Generate HTML table from ResultSet
            StringBuilder tableHtml = new StringBuilder("<table>");
            tableHtml.append("<tr>");
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                tableHtml.append("<th>").append(resultSet.getMetaData().getColumnName(i)).append("</th>");
            }
            tableHtml.append("</tr>");
            while (resultSet.next()) {
                tableHtml.append("<tr>");
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    tableHtml.append("<td>").append(resultSet.getString(i)).append("</td>");
                }
                tableHtml.append("</tr>");
            }
            tableHtml.append("</table>");
            return tableHtml.toString();
        }
    }
    static class ReportsPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Read the HTML file
            File htmlFile = new File("reports.html");
            byte[] htmlBytes = Files.readAllBytes(htmlFile.toPath());
            
            // Set the response headers
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            
            // Write the HTML content to the response body
            OutputStream os = exchange.getResponseBody();
            os.write(htmlBytes);
            os.close();
        }
    }
}
