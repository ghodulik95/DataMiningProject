
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
	
	public void removeCluster(){
		model.remove(0);
	}
	
	void addAllCells() {
		numCells = 0;
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
		int i = 0;
		for(Cluster c : model){
			totalCost += c.calcCost();
			//c.printAttr();
			//System.out.println("total"+(++i)+" :"+c.cells.size());
		}
		/*int totalNumCells = Cluster.original.numRows * Cluster.original.attributes.size();
		totalCost += (totalNumCells - numCells)*Cluster.averageCellCost;*/
		//System.out.println("total2 : "+totalCost);
		//2076808.422228242
		totalCost += (new NonClusterSpace(this)).calcCost();
		//System.out.println("done");
		//2062018.7929820728
		return totalCost;
	}
}
