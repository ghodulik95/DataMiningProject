import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;


public class Communicator {
		// JDBC driver name and database URL
	   public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   private String DB_URL;

	   //  Database credentials
	   private String user;
	   private String pass;
	   
	   private java.sql.Connection conn;
	   
	   public Communicator(String username, String password, String dbname){
		      user = username;
		      pass = password;
		      DB_URL = "jdbc:mysql://localhost/"+dbname;
	   }
	   
	   public boolean connect(){
		   try{
			   Class.forName("com.mysql.jdbc.Driver").newInstance();
			   conn = DriverManager.getConnection(DB_URL,user,pass);
		   }catch(Exception e){
			   e.printStackTrace();
			   return false;
		   }
		   return true;
	   }
	   
	   public ResultSet query(String query){
		   java.sql.Statement stmt = null;
		   try{
			   stmt = conn.createStatement();
			   ResultSet rs = stmt.executeQuery(query);
			   //stmt.close();
			   return rs;
		   }catch(Exception e){
			   try{
				   stmt.close();
			   }catch(Exception e2){
				   
			   }
			   e.printStackTrace();
			   return null;
		   }
	   }
	   
	   public static void printAll(ResultSet rs){
		   try{
			   ResultSetMetaData rsmd = rs.getMetaData();
			   int columnsNumber = rsmd.getColumnCount();
			   while (rs.next()) {
			       for (int i = 1; i <= columnsNumber; i++) {
			           String columnValue = rs.getString(i);
			           System.out.print(columnValue + " " + rsmd.getColumnName(i));
			       }
			       System.out.println("");
			   }
		   }catch(Exception e){
			   System.out.println("Print failed");
		   }
	   }
}
