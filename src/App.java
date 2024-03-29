import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;

public class App {
    public static void main(String[] args) throws IOException {
        // Start Swing GUI
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });

        // Start HTTP server in a separate thread
        new Thread(() -> {
            try {
                startHttpServer();
            } catch (IOException e) {
                Utils.logMessage(e.getMessage());
            }
        }).start();

        // Print startup message
        Utils.logMessage("Starting FRC Scouting App");
        Utils.logMessage("Server is running on port 8000...");
        Utils.logMessage(Constants.VersionInfo.verConsole);

        // Calculate and store averages
        AverageData.calculateAndStoreAverages();
        MatchData.publishMatchData();
    }
    
    private static void createAndShowGUI() {
        // Create JFrame
        JFrame frame = new JFrame("FRC Scouting App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create JTextArea for console
        JTextArea consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);

        // Create JScrollPane for console
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);

        // Create JPanel for buttons
        JPanel buttonPanel = new JPanel();
        JButton updateMatchDataButton = new JButton("Update Match Data");
        JButton updateTeamAveragesButton = new JButton("Update Team Averages");
        JButton refreshDataButton = new JButton("Refresh Data on API Page");
        buttonPanel.add(updateMatchDataButton);
        buttonPanel.add(updateTeamAveragesButton);
        buttonPanel.add(refreshDataButton);

        // Create JPanel for version info
        buttonPanel.add(new JLabel(Constants.VersionInfo.verConsole));

        // Add action listeners to buttons
        updateMatchDataButton.addActionListener(e -> {
            MatchData.publishMatchData();
            Utils.logMessage("Match data updated.");
        });

        updateTeamAveragesButton.addActionListener(e -> {
            AverageData.calculateAndStoreAverages();
            Utils.logMessage("Team averages updated.");
        });

        refreshDataButton.addActionListener(e -> {
            TeamList.TeamsHandler.updateAPIData();
            Utils.logMessage("Data on API page refreshed.");
        });

        // Add components to JFrame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Redirect console output to JTextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Display JFrame
        frame.setVisible(true);
    }



    private static void startHttpServer() throws IOException {
    	Utils.logMessage("Starting HTTP server on port 8000...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new DataEntry.DataEntryHandler());
        server.createContext("/team_averages.html", new AverageData.TeamAveragesHandler());
        server.createContext("/data_management.html", new Handlers.DataManagementHandler());
        server.createContext("/access_denied.html", new Handlers.AccessDenied());
        server.createContext("/script.js", new JSHandler.javascriptHandler());
        server.createContext("/actual_stats.html", new MatchData.statsHandler());
        server.createContext("/script-no-pwd.js", new JSHandler.javascriptHandler2());
        server.createContext("/admin.html", new PasswordAuthHandler());
        server.createContext("/teams.html",new TeamList.TeamsHandler());
        server.createContext("/pit-scout.html",new PitScout.DataEntryHandler());
        server.createContext("/style.css",new Handlers.CSSHandler());
        server.start();
    }
    
}

class CustomOutputStream extends OutputStream {
    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        // Redirects data to the JTextArea
        textArea.append(String.valueOf((char) b));
        // Scrolls the JTextArea to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
