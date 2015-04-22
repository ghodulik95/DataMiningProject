import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;


public class Communicator {
		// JDBC driver name and database URL
	   public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   public static final String DB_URL = "jdbc:mysql://localhost/dataminingproject";

	   //  Database credentials
	   public static final String USER = "root";
	   public static final String PASS = "changeme";
	   
	   private java.sql.Connection conn;
	   
	   public Communicator(){
		      
	   }
	   
	   public boolean connect(){
		   try{
			   Class.forName("com.mysql.jdbc.Driver").newInstance();
			   conn = DriverManager.getConnection(DB_URL,USER,PASS);
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
