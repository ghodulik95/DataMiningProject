import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.PriorityQueue;



public class ROCAT {
	public static int numCellsOverlap = 0;
	public static class pairOfClusters{
		public Cluster c1;
		public Cluster c2;
		public pairOfClusters(Cluster one, Cluster two){
			c1 = one;
			c2 = two;
		}
	}
	
	public static List<Cluster> rocat(Cluster d, String procOutput){
		File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, procOutput+".txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());
		
		
			double cost = Double.POSITIVE_INFINITY;
			System.out.println("Orig cost "+cost);
			Model subClus = new Model(new ArrayList<Cluster>());
			LinkedList<Cluster> queue = new LinkedList<Cluster>();
			queue.push(d);
			int round = 1;
			System.out.println("Begin Searching Phase");
			outputWriter.println("Begin Searching Phase");
			while(!queue.isEmpty()){
				Cluster curr = queue.pop();
				Cluster c = findBestPure(curr, subClus, outputWriter);
				if(c == null || c.numRows <= 0 || c.cells.size() <= 0){
					continue;
				}
				
				subClus.addCluster(c);
				subClus.addAllCells();
				double curCost = subClus.calcCost();
				if(curCost < cost){
					System.out.println("Round "+(round)+", ADDED cluster: "+c.attributes);
					outputWriter.println("Round "+(round++)+", ADDED cluster: "+c.attributes);
					cost = curCost;
					queue.addAll(splitSpace(curr, c));
				}else{
					subClus.removeCluster();
				}
			}
			
			System.out.println("Model:");
			outputWriter.println("Model:");
			int i = 1;
			for(Cluster c : subClus.model){
				System.out.println("Cluster "+i+":"+c.attributes);
				outputWriter.println("Cluster "+i+":"+c.attributes);
			}
			System.out.println("Searching Phase completed");
			outputWriter.println("Searching Phase completed");
			
			List<pairOfClusters> overlaps = new ArrayList<pairOfClusters>();
			for(i = 0; i < subClus.model.size(); i++){
				for(int j = i+1; j < subClus.model.size(); j++){
					if(overlapping(subClus.model.get(i), subClus.model.get(j), outputWriter)){
						overlaps.add(new pairOfClusters(subClus.model.get(i), subClus.model.get(j)));
					}
				}
			}
			System.out.println(overlaps.size()+" clusters overlap");
			outputWriter.println(overlaps.size()+" clusters overlap");
			if(!overlaps.isEmpty()){
				System.out.println("Skipping combining phase");
				outputWriter.println("Skipping combining phase");
			}else{
				System.out.println("Combining Phase not implemented - this could be a problem");
				outputWriter.println("Combining Phase not implemented - this could be a problem");
			}
			System.out.println("Beginning Reassigning Phase");
			outputWriter.println("Beginning Reassigning Phase");
			round = 1;
			boolean changed = true;
			while(changed){
				changed = false;
				Set<Integer> changedClusters = new HashSet<Integer>();
				for(i = 0; i <subClus.model.size() ; i++){
					Cluster c = subClus.model.get(i);
					System.out.println("Round "+(round)+" : Cluster "+(i+1));
					outputWriter.println("Round "+(round)+" : Cluster "+(i+1));
					for(Entry<Integer, Map<Column, Cell>> row : Cluster.original.cells.entrySet()){
						if(c.cells.containsKey(row.getKey())){
							c.removeRow(row.getKey());
							subClus.removeRow(row);
							double curCost = subClus.calcCost();
							if(curCost < cost){
								cost = curCost;
								System.out.println("Cluster "+(i+1)+" removed a row: "+c.attributes);
								outputWriter.println("Cluster "+(i+1)+" removed a row: "+c.attributes);
								changed = true;
								changedClusters.add(i);
								
							}else{
								c.addRow(row);
								subClus.addCellsBack(row);
							}
						}else{
							List<Cell> addedToClus = c.addRow(row);
							if(addedToClus != null){
								subClus.addCells(addedToClus);
								double curCost = subClus.calcCost();
								if(curCost < cost){
									cost = curCost;
									System.out.println("Cluster "+(i+1)+" added a row: "+c.attributes);
									outputWriter.println("Cluster "+(i+1)+" added a row: "+c.attributes);
									changed = true;
									changedClusters.add(i);
								}else{
									c.removeRow(row.getKey());
									subClus.removeCellsBack();
								}
							}
						}
					}
				}
				Cluster c;
				for(Integer ind : changedClusters){
					c = subClus.model.get(ind);
					System.out.println("Round "+(round)+" : Cluster "+(ind+1));
					outputWriter.println("Round "+(round)+" : Cluster "+(ind+1));
					Set<Column> triedAttr = new HashSet<Column>();
					boolean addedAttr = true;
					while(addedAttr){
						Column a = getBestColumn(Cluster.original, c, getAttributesNotInCluster(c,triedAttr));
						triedAttr.add(a);
						List<Cell> addedToClus = c.addColumn(a);
						subClus.addCells(addedToClus);
						double curCost = subClus.calcCost();
						if(curCost < cost){
							cost = curCost;
							addedAttr = true;
							System.out.println("Cluster "+(ind + 1)+" added attr "+a.attrName+":"+c.attributes);
							outputWriter.println("Cluster "+(ind + 1)+" added attr "+a.attrName+":"+c.attributes);
						}else{
							subClus.removeCellsBack();
							c.removeCells(addedToClus);
							addedAttr = false;
						}
					}
				}
				round++;		
			}
			System.out.println("Finished with ROCAT");
			outputWriter.println("Finished with ROCAT");
		
			overlaps = new ArrayList<pairOfClusters>();
			for(i = 0; i < subClus.model.size(); i++){
				for(int j = i+1; j < subClus.model.size(); j++){
					if(overlapping(subClus.model.get(i), subClus.model.get(j), outputWriter)){
						overlaps.add(new pairOfClusters(subClus.model.get(i), subClus.model.get(j)));
					}
				}
			}
			System.out.println(overlaps.size()+" clusters overlap\n"+numCellsOverlap+" cells overlap");
			outputWriter.println(overlaps.size()+" clusters overlap\n"+numCellsOverlap+" cells overlap");
			
			return subClus.model;
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
	
	private static Iterator<Column> getAttributesNotInCluster(Cluster c, Set<Column> triedAttr) {
		List<Column> attr = new ArrayList<Column>();
		for(Column a : Cluster.original.attributes){
			if(!c.attributes.contains(a) && !triedAttr.contains(a)){
				attr.add(a);
			}
		}
		return attr.iterator();
	}

	private static boolean overlapping(Cluster c1, Cluster c2, PrintWriter outputWriter) {
		boolean colsOverlap = false;
		int numAttrShare = 0;
		for(Column col1 : c1.attributes){
			for(Column col2 : c2.attributes){
				if(col1.equals(col2)){
					colsOverlap = true;
					numAttrShare++;
				}
			}
		}
		if(numAttrShare == 0){
			return false;
		}
		int numRowsOverlap = 0;
		List<Integer> rowIds = new ArrayList<Integer>();
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
					rowIds.add(rowId);
					numRowsOverlap++;
				}
			}
		}
		if(numRowsOverlap > 0){
			numCellsOverlap += numRowsOverlap*numAttrShare;
			outputWriter.println("Clusters overlap: \n"+c1.attributes+"\n"+c2.attributes);
			outputWriter.println("At rows: "+rowIds);
		}
		return numRowsOverlap > 0;
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
	
    		PriorityQueue<Column> attr = new PriorityQueue<Column>(new EntropyComparator());
    		attr.addAll(m.attributes);
    		double lowestCost = Double.POSITIVE_INFINITY;
    		Cluster best = null;
    		Cluster prev = null;
    		while(!attr.isEmpty()){
    			//Get a with min entropy
    			Column a;
    			if(prev == null){
    				a = attr.poll();
    			}else{
    				a = getBestColumn(m, prev, attr.iterator());
    				attr.remove(a);
    			}
    			if(prev == null){
    				prev = Cluster.cutPureAttr(m, a);
    			}else{
    				prev = Cluster.makeBiggest(m, prev, a);
    			}
    			
    			if(prev.numRows <= 1  || prev.cells.size() <= 1){
    				return best;
    			}
    			
    			subClus.addCluster(prev);
    			subClus.addAllCells();
    			double cost = subClus.calcCost();
    			System.out.println(cost);
    			
    			if(Double.isNaN(cost)){
    				return best;
    			}
    			
    			if(cost < lowestCost){
    				lowestCost = cost;
    				best = prev;
    			}

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
				return a;
			}
			else if(entropy < lowestEntropy){
				lowestEntropy = entropy;
				bestCol = a;
			}
		}
		return bestCol;
	}
}
