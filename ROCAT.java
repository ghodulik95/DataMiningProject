import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.PriorityQueue;



public class ROCAT {
	
	public static List<Cluster> rocat(Cluster d){
		File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());
		
		
			double cost = Double.POSITIVE_INFINITY;//d.calcCost();//getOriginalCost(d);
			System.out.println("Orig cost "+cost);
			Model subClus = new Model(new ArrayList<Cluster>());
			LinkedList<Cluster> queue = new LinkedList<Cluster>();
			queue.push(d);
			while(!queue.isEmpty()){
				Cluster curr = queue.pop();
				System.out.println("current subspace # rows: "+curr.numRows);
				System.out.println("Number of relevant clusters: "+subClus.model.size());
				Cluster c = findBestPure(curr, subClus, outputWriter);
				if(c == null || c.numRows <= 1 || c.cells.size() <= 1){
					continue;
				}
				
				subClus.addCluster(c);
				subClus.addAllCells();
				double curCost = subClus.calcCost();
				//System.out.println("New cost "+curCost);
				if(curCost < cost){
					System.out.println("ADDED: "+c.attributes);
					cost = curCost;
					queue.addAll(splitSpace(curr, c));
				}else{
					System.out.println("DID NOT ADD: "+c.attributes);
					subClus.removeCluster();
				}
				
			}
			
			return subClus.model;
		
        //to here
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (outputWriter != null)
	            outputWriter.close();
	    }
		
		return null;
	}
	
	private static double getOriginalCost(Cluster d) {
		ArrayList<Cluster> t = new ArrayList<Cluster>();
		t.add(d);
		Model tm = new Model(t);
		return tm.calcCost();
	}

	private static List<Cluster> splitSpace(Cluster curr, Cluster c) {
		ArrayList<Cluster> ret = new ArrayList<Cluster>();
		ret.add(c.getComplement(curr));
		Cluster r2 = new Cluster(curr.numRows - c.numRows);
		for(Integer rowId : curr.cells.keySet()){
			if(!c.cells.containsKey(rowId)){
				Map<Column, Cell> row = new HashMap<Column, Cell>();
				for(Entry<Column, Cell> e : curr.cells.get(rowId).entrySet()){
					row.put(e.getKey(), e.getValue());
				}
				r2.cells.put(rowId, row);
			}
		}
		r2.setAttributes();
		ret.add(r2);
		return ret;
	}

	public static Cluster findBestPure(Cluster m, Model subClus, PrintWriter outputWriter){
	
            //here
            //List<Cluster> pure = new ArrayList<Cluster>();
    		PriorityQueue<Column> attr = new PriorityQueue<Column>(new EntropyComparator());
    		attr.addAll(m.attributes);
    		List<Column> attrPrime = new ArrayList<Column>();
    		double lowestCost = Double.POSITIVE_INFINITY;
    		Cluster best = null;
    		Cluster prev = null;
    		boolean first = true;
    		while(!attr.isEmpty()){
    			//Get a with min entropy
    			Column a = attr.poll();
    			System.out.println("Got column "+a);
    			if(prev == null){
    				prev = Cluster.cutPureAttr(m, a);
    				//pure.add(t);
    			}else{
    				prev = Cluster.makeBiggest(m, prev, a);
    				//pure.add(t);
    			}
    			
    			if(prev.numRows <= 1  || prev.cells.size() <= 1){
    				return best;
    			}
    			attrPrime.add(a);
    			//System.out.println(prev.numRows + " - "+prev.attributes);
    			//outputWriter.println(prev.numRows+" - "+prev.attributes);
    			
    			
    			subClus.addCluster(prev);
    			subClus.addAllCells();
    			double cost = subClus.calcCost();
    			System.out.println(cost);
    			
    			if(Double.isNaN(cost)){
    				return best;
    			}
    			
    			if(/*!first && */cost < lowestCost){
    				lowestCost = cost;
    				best = prev;
    				System.out.println("NEW BEST! -- "+best.cells.size() + "   --   "+best.numRows);
    	            //outputWriter.println("NEW BEST! -- "+best.cells.size() + "   --   "+best.numRows);
    			}else if(!first){
    				//return best;
    			}

    			first = false;
    			//System.out.println("BEST : "+(best == null? "NULL NOW" : best.attributes));
    			subClus.removeCluster();
    		}
    		return best;
				
	}
}
