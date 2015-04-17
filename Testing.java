import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;


public class Testing {
	public static void main(String args[]) throws SQLException, FileNotFoundException{
		Cluster c = Cluster.clusterFromQuery("select *  ",
						 " FROM trim3 ", " LIMIT 100");
		List<Cluster> r = ROCAT.rocat(c);
		int i = 1;
		for(Cluster clus : r){
			System.out.println("Cluster "+(i++)+": "+clus.attributes);
		}
		//p.printCells();
		/*Communicator com = new Communicator();
		com.connect();
		ResultSet rs = com.query("SELECT paper FROM min_trim");
		String r = "";
		while(rs.next()){
			r += rs.getInt("paper");
		}
		rs.close();
		PrintWriter out = new PrintWriter("paper.txt");
		out.println(r);
		out.close();*/
		/*Communicator com = new Communicator();
		com.connect();
		ResultSet rs = com.query("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS  WHERE table_name = 'trim2'  ORDER BY ordinal_position");
		while(rs.next()){
			String col = rs.getString(1);
			ResultSet r = com.query("Select "+col+", COUNT(*)/4952 as prob from trim2 group by "+col +" order by prob desc");
			r.next();
			double pr = r.getDouble("prob");
			if(pr > 0.9){
				System.out.println("DROP COLUMN "+col+",");
			}
			r.close();
		}
		rs.close();*/
	}
}
