public class Constants {
	public static final class JDBCConstants{
		public static String serverIP,sqlPort,dbName,encrypt;
	    public static String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
	    public static String url2 = "jdbc:sqlserver://"+serverIP +":"+sqlPort +";databaseName="+dbName +";encrypt=false";
	    public static String username;
	    public static String password;
	}
	public static final class PasswordConstants{
		public static String PASSWORD;
		public static String PASSWORD_ADMIN;
		public static String APIKEY;
	}
	public static final class VersionInfo{
		public static String verConsole1;
	}
	public static final class TBA_API{
		public static String TBA_EVENT;
	}
}