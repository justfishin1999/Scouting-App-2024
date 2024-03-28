import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JSHandler {
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
}