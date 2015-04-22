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
	}
}
