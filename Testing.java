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
		Cluster c = Cluster.clusterFromQuery("SELECT * ", "FROM trim2", " ");
		List<Cluster> r = ROCAT.rocat(c);
		int i = 1;
		for(Cluster clus : r){
			System.out.println("Cluster "+(i++));
			clus.printAttr();
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
	}
}
