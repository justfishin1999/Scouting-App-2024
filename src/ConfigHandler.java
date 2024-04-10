import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigHandler {
	static String fileName = "config.txt";
	FileReader fileReader;
	
	public static void readConfigFile() throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;
			while ((line = br.readLine()) != null){
				String[] parts = line.split("=");
				if(parts.length==2) {
					String key = parts[0].trim();
					String value = parts[1].replaceAll("\"", "").trim();
					if(key.equals("serverVersion")) {
						Constants.VersionInfo.verConsole1 = "Server Version: "+value;
						Utils.logMessage("Server Version: "+value);
					}
					if(key.equals("eventkey")) {
						Constants.TBA_API.TBA_EVENT = value;
						Utils.logMessage("Event Key: "+value);
					}
					if (key.equals("adminPwd")) {
						Constants.PasswordConstants.PASSWORD_ADMIN = value;
					}
					if (key.equals("apiKey")) {
						Constants.PasswordConstants.APIKEY = value;
						Utils.logMessage("API Key: " + value);
					}
					if (key.equals("webPwd")) {
						Constants.PasswordConstants.PASSWORD = value;
					}
					if (key.equals("serverIP")) {
						Constants.JDBCConstants.serverIP = value;
					}
					if (key.equals("sqlPort")) {
						Constants.JDBCConstants.sqlPort = value;
					}
					if (key.equals("dbName")) {
						Constants.JDBCConstants.dbName = value;
					}
					if (key.equals("sqlUsr")) {
						Constants.JDBCConstants.username = value;
					}
					if (key.equals("sqlPwd")) {
						Constants.JDBCConstants.password = value;
					}
				}
			}
		} catch (IOException e) {
			Utils.logMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
