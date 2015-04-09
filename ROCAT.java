import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class ROCAT {
	public static Cluster findBestPure(Cluster m, List<Cluster> subClus){
		List<Cluster> pure = new ArrayList<Cluster>();
		PriorityQueue<Column> attr = new PriorityQueue<Column>(new EntropyComparator());
		attr.addAll(m.attributes);
		List<Column> attrPrime = new ArrayList<Column>();
		while(pure.size() < m.attributes.size()){
			//Get a with min entropy
			Column a = attr.poll();
			pure.add(Cluster.makeBiggest(m, pure.get(pure.size() - 1), a));
			attrPrime.add(a);
		}
		
	}
}
