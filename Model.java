
import java.util.List;


public class Model {
	public List<Cluster> model;
	public int numCells = 0;
	
	public Model(List<Cluster> m){
		model = m;
		addAllCells();
	}
	
	public void addCluster(Cluster c){
		model.add(c);
	}
	
	public void removeCluster(Cluster c){
		model.remove(c);
	}
	
	void addAllCells() {
		for(Cluster c : model){
			for(Integer rowId : c.cells.keySet()){
				for(Column a : c.cells.get(rowId).keySet()){
					numCells++;
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
		int totalNumCells = Cluster.original.numRows * Cluster.original.attributes.size();
		totalCost += (totalNumCells - numCells)*Cluster.averageCellCost;
		System.out.println("total : "+totalCost);
		return totalCost;
	}
}
