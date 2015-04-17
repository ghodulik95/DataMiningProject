import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.PriorityQueue;



public class ROCAT {
	
	public static class pairOfClusters{
		public Cluster c1;
		public Cluster c2;
		public pairOfClusters(Cluster one, Cluster two){
			c1 = one;
			c2 = two;
		}
	}
	
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
			int round = 1;
			while(!queue.isEmpty()){
				Cluster curr = queue.pop();
				System.out.println("current subspace # rows: "+curr.numRows);
				System.out.println("Number of relevant clusters: "+subClus.model.size());
				System.out.println("Length of queue: "+queue.size());
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

				System.out.println("Round "+(round++));
				for(Cluster clus : subClus.model){
					System.out.println(clus.attributes);
				}
				
			}
			Serializer serial = new Serializer();
			serial.serializeClusters(subClus.model);
			subClus.model = serial.deserializeClusters();
			
			List<pairOfClusters> overlaps = new ArrayList<pairOfClusters>();
			for(int i = 0; i < subClus.model.size(); i++){
				for(int j = i+1; j < subClus.model.size(); j++){
					if(overlapping(subClus.model.get(i), subClus.model.get(j))){
						overlaps.add(new pairOfClusters(subClus.model.get(i), subClus.model.get(j)));
					}
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
	
	private static void processPairs(int i, int j, Model subClus) {
		Cluster c1 = subClus.model.get(i);
		Cluster c2 = subClus.model.get(j);
		if(overlapping(c1,c2)){
			return;
		}else{
			
		}
	}

	private static boolean overlapping(Cluster c1, Cluster c2) {
		boolean colsOverlap = false;
		colsCheck:
		for(Column col1 : c1.attributes){
			for(Column col2 : c2.attributes){
				if(col1.equals(col2)){
					colsOverlap = true;
					break colsCheck;
				}
			}
		}
		if(colsOverlap){
			Cluster itOver;
			Cluster checking;
			if(c1.numRows > c2.numRows){
				itOver = c2;
				checking = c1;
			}else{
				itOver = c1;
				checking = c2;
			}
			for(Integer rowId : itOver.cells.keySet()){
				if(checking.cells.containsKey(rowId)){
					return true;
				}
			}
		}
		return false;
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
    			Column a;
    			if(prev == null){
    				a = attr.poll();
    			}else{
    				a = getBestColumn(m, prev, attr.iterator());
    				attr.remove(a);
    			}
    			System.out.println("Got column "+a);
    			if(prev == null){
    				prev = Cluster.cutPureAttr(m, a);
    				//pure.add(t);
    			}else{
    				prev = Cluster.makeBiggest(m, prev, a);
    				//pure.add(t);
    			}
    			
    			if(prev.numRows <= 25  || prev.cells.size() <= 25){
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

	private static Column getBestColumn(Cluster m, Cluster prev, Iterator<Column> iterator) {
		double lowestEntropy = Double.POSITIVE_INFINITY;
		Column bestCol = null;
		while(iterator.hasNext()){
			Column a = iterator.next();
			Collection<Integer> valDist = Cluster.findValueDistribution(m, a, prev.cells.keySet());
			double entropy = Column.calcEntropy(valDist, prev.numRows);
			if(entropy == 0.0){
				//System.out.print("Best entr: "+entropy+" ");
				return a;
			}
			else if(entropy < lowestEntropy){
				lowestEntropy = entropy;
				bestCol = a;
			}
		}
		System.out.print("Best entr: "+lowestEntropy+" ");
		return bestCol;
	}
}
