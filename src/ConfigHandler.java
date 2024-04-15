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
						Constants.VersionInfo.verConsole1 = value;
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
						Constants.JDBCConstants.SERVER_IP = value;
					}
					if (key.equals("sqlPort")) {
						Constants.JDBCConstants.SQL_PORT = value;
					}
					if (key.equals("dbName")) {
						Constants.JDBCConstants.DB_NAME = value;
					}
					if (key.equals("sqlUsr")) {
						Constants.JDBCConstants.USERNAME = value;
					}
					if (key.equals("sqlPwd")) {
						Constants.JDBCConstants.PASSWORD = value;
					}
					if (key.equals("statsTeam")) {
						Constants.TBA_API.STAT_TEAM = value;
					}
				}
			}
		} catch (IOException e) {
			Utils.logMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
