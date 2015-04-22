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


public class Testing {
	public static void main(String args[]) throws SQLException, FileNotFoundException{
		Scanner sn = new Scanner(System.in);
		System.out.println("Please give the tablename name: ");
		String tname = sn.nextLine();
		System.out.println("Please give attribute to test class: ");
		String attrToTest = sn.nextLine();
		System.out.println("Please give cluster output file: ");
		String clusOutput = sn.nextLine();
		System.out.println("Please give process output file: ");
		String procOutput = sn.nextLine();
		

        Communicator comm = new Communicator();
        comm.connect();
        if(null == comm.query("SELECT "+attrToTest+" FROM scaled1 LIMIT 1")){
        	System.out.println("Invalid attr "+attrToTest);
        	return;
        }
        
		
		Cluster c = Cluster.clusterFromQuery("select *  ",
						 " FROM "+tname+" ", " ");
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
	    				ResultSet res = comm.query("SELECT "+attrToTest+" FROM scaled1 where min_trim_id = "+rowId);
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
