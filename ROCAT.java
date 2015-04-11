import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class ROCAT {
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
			System.out.println("Got column "+a);
			if(prev == null){
				prev = Cluster.cutPureAttr(m, a);
				//pure.add(t);
			}else{
				prev = Cluster.makeBiggest(m, prev, a);
				//pure.add(t);
			}
			
			if(prev.numRows == 1){
				return best;
			}
			//System.out.println(prev.numRows);
			attrPrime.add(a);
			
			subClus.addCluster(prev);
			subClus.addAllCells();
			double cost = subClus.calcCost();
			System.out.println("Cost is "+cost);
			
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
			System.out.println("BEST : "+(best == null? "NULL NOW" : best.attributes));
			subClus.removeCluster();
		}
		return best;
		
	}
}
