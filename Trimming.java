
//STEP 1. Import required packages
import java.sql.*;

public class Trimming {

   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/dataminingproject";

   //  Database credentials
   static final String USER = "george";
   static final String PASS = "1234";
	public static void main(String[] args){
	   
	   Connection conn = null;
	   Statement stmt = null;
	   try{
	      //STEP 2: Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver").newInstance();

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL,USER,PASS);

	      //STEP 4: Execute a query
	      System.out.println("Creating statement...");
	      stmt = conn.createStatement();
	      String sql;
	      sql = "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'min_trim'  ORDER BY ordinal_position";
	      ResultSet rs = stmt.executeQuery(sql);
	      rs.next(); rs.next(); rs.next();
	      //STEP 5: Extract data from result set
	      while(rs.next()){
	         //Retrieve by column name
	         String attr  = rs.getString("column_name");

	         //System.out.println("Attr: "+attr);
	         sql = "SELECT "+attr+", COUNT(*) / 4952 as cnt FROM min_trim GROUP BY "+attr;
	         ResultSet rs2 = conn.createStatement().executeQuery(sql);
	         while(rs2.next()){
	        	 double cnt = rs2.getDouble("cnt");
	        	 if(cnt > 0.9){
	        		 System.out.println(attr+" Lots!");
	        		 continue;
	        	 }/*
	        	 try{
		        	 int value = rs2.getInt(attr);
		        	 System.out.println("\tValue "+value+": "+cnt);
	        	 }catch(java.sql.SQLException e){
	        		 String value = rs2.getString(attr);
	        		 System.out.println("\tValue "+value+": "+cnt);
	        	 }*/
	        	 //to delete
	        	 //PRIsland -- too much missing - data assume no?
	        	 //g5c - arrested by police -- missing, assume no?
	        	 //q23a2 all blanked
	        	 //q23a3 missing
	        	 //q23a4 blanked
	        	 // OK re_18i1 citizenship missing, assume citizen?
	        	 // OK re_Q18A6 gender identity missing - assume cis?
	        	 //re_18c1 missing sexual identitu
	        	 //zcta blanked
	        	 //age18 blaned
	         }
	      }
	      /*
	      PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM video_games WHERE id = ?");
	      pstmt.setInt(1, 1);
	      rs = pstmt.executeQuery();
	      
	      while(rs.next()){
	         //Retrieve by column name
	         int id  = rs.getInt("id");

	         //Display values
	         System.out.println("ID: " + id);
	      }
	      
	      //STEP 6: Clean-up environment
*/	      rs.close();
	      stmt.close();
	      conn.close();
	   }catch(SQLException se){
	      //Handle errors for JDBC
	      se.printStackTrace();
	   }catch(Exception e){
	      //Handle errors for Class.forName
	      e.printStackTrace();
	   }finally{
	      //finally block used to close resources
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	      }// nothing we can do
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }//end finally try
	   }//end try
	   System.out.println("Goodbye!");
	}
}
