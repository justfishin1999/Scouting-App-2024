import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigHandler {
	static String fileName = "config.txt";
	public static String form1Value,form2Value,form3Value,form4Value,form5Value,form6Value,form7Value,form8Value = null;
	FileReader fileReader;
	
	public static void readConfigFile() throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;
			while ((line = br.readLine()) != null){
				String[] parts = line.split("=");
				if(parts.length==2) {
					String key = parts[0].trim();
					String value = parts[1].replaceAll("\"", "").trim();
					
					if(key.equals("form1")) {
						form1Value = value;
					}
					if(key.equals("form2")) {
						form2Value = value;
					}
					if(key.equals("form3")) {
						form3Value = value;
					}
					if(key.equals("form4")) {
						form4Value = value;
					}
					if(key.equals("form5")) {
						form5Value = value;
					}
					if(key.equals("form6")) {
						form6Value = value;
					}
					if(key.equals("form7")) {
						form7Value = value;
					}
					if(key.equals("form8")) {
						form8Value = value;
					}
				}
			}
		} catch (IOException e) {
			Utils.logMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
