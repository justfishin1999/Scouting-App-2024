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
        server.createContext("/script.js", new JSHandler.javascriptHandler());
        server.createContext("/actual_stats.html", new Handlers.statsHandler());
        server.createContext("/script-no-pwd.js", new JSHandler.javascriptHandler2());
        server.createContext("/admin.html", new PasswordAuthHandler());
        server.start();
        System.out.println("**********************************");
        System.out.println("Starting FRC Scouting App");
        System.out.println("**********************************");
        System.out.println("Server is running on port 8000...");
        System.out.println("Server version v0.1.1 - beta");
        System.out.println("**********************************");

        // Calculate and store averages
        AverageData.calculateAndStoreAverages();
        MatchData.publishMatchData();        
    }
}
