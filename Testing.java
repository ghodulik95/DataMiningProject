import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


public class Testing {
	public static void main(String args[]) throws SQLException, FileNotFoundException{
		Model m = new Model(new LinkedList<Cluster>());
		Cluster c = Cluster.clusterFromQuery("SELECT * ", "FROM trim2", " ");
		Cluster p = ROCAT.findBestPure(c, m);
		p.printAttr();
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
