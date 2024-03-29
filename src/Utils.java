import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
    public static String convertStreamToString(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
    /*public static int parseOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        // Check if the value contains a decimal point
        int decimalIndex = value.indexOf('.');
        if (decimalIndex != -1) {
            // Remove everything after the decimal point and parse the integer
            value = value.substring(0, decimalIndex);
        }
        return Integer.parseInt(value);
    }*/
    public static int parseOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        // Check if the value contains a decimal point
        int decimalIndex = value.indexOf('.');
        if (decimalIndex != -1) {
            // Remove everything after the decimal point and parse the integer
            value = value.substring(0, decimalIndex);
        }
        // Parse the integer value
        int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // If parsing fails, return the default value
            return defaultValue;
        }
        // Set any value that is more than zero but less than 1 to be zero
        if (intValue > 0 && intValue < 1) {
            return 0;
        }
        return intValue;
    }



}
