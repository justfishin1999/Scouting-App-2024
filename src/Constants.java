public class Constants {
	public static final class JDBCConstants{
		public static String SERVER_IP,SQL_PORT,DB_NAME;
	    public static String url = "jdbc:sqlserver://localhost:1433;databaseName=Scout2024;encrypt=false";
	    public static String URL2 = "jdbc:sqlserver://"+SERVER_IP +":"+SQL_PORT +";databaseName="+DB_NAME +";encrypt=false";
	    public static String USERNAME;
	    public static String PASSWORD;
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
		public static String STAT_TEAM;
	}
}