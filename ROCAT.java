import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;


public class ROCAT {
	
	public static List<Cluster> rocat(Cluster d){
		double cost = getOriginalCost(d);
		System.out.println("Orig cost "+cost);
		Model subClus = new Model(new ArrayList<Cluster>());
		LinkedList<Cluster> queue = new LinkedList<Cluster>();
		queue.push(d);
		while(!queue.isEmpty()){
			Cluster curr = queue.pop();
			System.out.println("cur "+curr.numRows);
			System.out.println("sub "+subClus.model.size());
			Cluster c = findBestPure(curr, subClus);
			if(c.numRows <= 1 || c.cells.size() <= 1){
				continue;
			}
			
			subClus.addCluster(c);
			subClus.addAllCells();
			double curCost = subClus.calcCost();
			System.out.println("New cost "+curCost);
			if(curCost < cost){
				cost = curCost;
				queue.addAll(splitSpace(curr, c));
			}else{
				subClus.removeCluster();
			}
			
		}
		return subClus.model;
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

	public static Cluster findBestPure(Cluster m, Model subClus){
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
			//System.out.println("Got column "+a);
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
			System.out.println(prev.numRows);
			attrPrime.add(a);
			
			subClus.addCluster(prev);
			subClus.addAllCells();
			double cost = subClus.calcCost();
			//System.out.println("Cost is "+cost);
			
			if(Double.isNaN(cost)){
				return best;
			}
			
			if(!first && cost < lowestCost){
				lowestCost = cost;
				best = prev;
				System.out.println("NEW BEST! -- "+best.cells.size() + "   --   "+best.numRows);
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
