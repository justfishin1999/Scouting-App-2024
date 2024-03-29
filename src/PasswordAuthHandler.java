import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class PasswordAuthHandler implements HttpHandler {

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
        // Read admin.html and serve its contents
        String response = readFile("admin.html");
        sendResponse(exchange, 200, "OK", response);
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        // Get the request body input stream
        InputStream requestBodyStream = exchange.getRequestBody();

        // Read the request body byte by byte until finding the password parameter
        StringBuilder requestBodyBuilder = new StringBuilder();
        int byteRead;
        while ((byteRead = requestBodyStream.read()) != -1) {
            requestBodyBuilder.append((char) byteRead);
        }

        // Convert the request body to a string
        String requestBody = requestBodyBuilder.toString();

        // Split the request body to extract the password parameter
        String[] params = requestBody.split("&");
        String password = null;

        // Extract the password parameter
        for (String param : params) {
            String[] keyValue = param.split("=");
            if ("password".equals(keyValue[0])) {
                password = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                break;
            }
        }

        if (password != null && Constants.PasswordConstants.PASSWORD.equals(password)) {
            // Password is correct, redirect to data_management.html
        	String redirectResponse = "<html><head><meta http-equiv='refresh' content='0; url=/data_management.html?source=uo78t6irtdyugiuo6itdycygioftdiyrckgvlyfuotdiyrxfjc'></head><body></body></html>";
            sendResponse(exchange, 302, "Found", redirectResponse);
        } else {
            // Password is incorrect
            sendResponse(exchange, 401, "Unauthorized", "Incorrect password");
        }
    }




    private void sendResponse(HttpExchange exchange, int statusCode, String statusMessage, String responseText) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
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
