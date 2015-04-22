import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;


public class Runner {
	public static void main(String args[]) throws SQLException, FileNotFoundException{

		System.out.println("Welcome to ROCAT application.  You are expected to have a MySQL server ");
		System.out.println("installed at localhost.");
		Scanner sn = new Scanner(System.in);
		System.out.println("Please give your MySQL username: ");
		String uname = sn.nextLine();
		System.out.println("Please give your MySQL password: ");
		String pass = sn.nextLine();
		System.out.println("Please give the database name: ");
		String dbname = sn.nextLine();
		System.out.println("Please give the clustering tablename name: ");
		String tname = sn.nextLine();
		System.out.println("Please give attribute to test class (NONE if not testing attribute): ");
		String attrToTest = sn.nextLine();
		System.out.println("Please give the classifying table name (disregard if not testing attribute): ");
		String origTable = sn.nextLine();
		System.out.println("Please give the name of the id attribute in the classifying table (disregard if not testing attribute): ");
		String idColumn = sn.nextLine();
		System.out.println("Please give cluster output file: ");
		String clusOutput = sn.nextLine();
		System.out.println("Please give process output file: ");
		String procOutput = sn.nextLine();
		

        Communicator comm = new Communicator(uname, pass, dbname);
        if(!comm.connect()){
        	System.out.println("Failed to connect to "+dbname+" with credentials "+uname+", "+pass );
        	return;
        }
        if(null == comm.query("SELECT "+attrToTest+" FROM "+origTable+" LIMIT 1")){
        	System.out.println("Invalid attr "+attrToTest);
        	return;
        }
        if(null == comm.query("SELECT "+idColumn+" FROM "+origTable+" LIMIT 1")){
        	System.out.println("Invalid attr "+attrToTest);
        	return;
        }
        
		
		Cluster c = Cluster.clusterFromQuery("select *  ",
						 " FROM "+tname+" ", " ", uname, pass, dbname);
		List<Cluster> r = ROCAT.rocat(c, procOutput);
		int i = 1;
		File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, clusOutput+".txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

    		for(Cluster clus : r){
    			outputWriter.println("Cluster "+(i++)+": "+clus.attributes);
    			if(!attrToTest.equals("NONE")){
	    			Map<String, Integer> classifications = new HashMap<String, Integer>();
	    			for(Integer rowId : clus.cells.keySet()){
	    				ResultSet res = comm.query("SELECT "+attrToTest+" FROM "+origTable+" where "+idColumn+" = "+rowId);
	    				res.next();
	    				String cl = res.getString(1);
	    				if(classifications.containsKey(cl)){
	    					classifications.put(cl, classifications.get(cl) + 1);
	    				}else{
	    					classifications.put(cl, 1);
	    				}
	    			}
	    			for(Entry<String,Integer> e : classifications.entrySet()){
	    				outputWriter.printf(e.getKey()+": "+e.getValue()+" = %.2f %%\n", (((e.getValue()*1.0)/clus.numRows)*100));
	    			}
	    			outputWriter.println();
    			}
    		}
    		
    		
            
        } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (outputWriter != null)
	            outputWriter.close();
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
