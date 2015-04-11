import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class Model {
	public List<Cluster> model;
	public Map<String, Integer> hasCells;
	
	public Model(List<Cluster> m){
		model = m;
		hasCells = new HashMap<String, Integer>();
		addAllCells();
	}
	
	public void addCluster(Cluster c){
		model.add(c);
	}
	
	public void removeCluster(Cluster c){
		model.remove(c);
	}
	
	void addAllCells() {
		hasCells.clear();
		for(Cluster c : model){
			for(Integer rowId : c.cells.keySet()){
				for(Column a : c.cells.get(rowId).keySet()){
					hasCells.put(a.attrName, rowId);
				}
			}
		}
	}

	public double calcCost(){
		double totalCost = 0.0;
		for(Cluster c : model){
			totalCost += c.calcCost();
			System.out.println("total : "+totalCost);
		}
		int numCells = Cluster.original.numRows * Cluster.original.attributes.size();
		totalCost += (numCells - hasCells.size())*Cluster.averageCellCost;
		System.out.println("total : "+totalCost);
		return totalCost;
	}
}
