
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Model {
	public List<Cluster> model;
	public int numCells = 0;
	public NonClusterSpace ns;
	
	public Model(List<Cluster> m){
		model = m;
		addAllCells();
		ns = new NonClusterSpace();
	}
	
	public void addCluster(Cluster c){
		model.add(c);
		ns.addCluster(c);
	}
	
	public void removeCluster(){
		model.remove(model.size() - 1);
		ns.removeCluster();
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
			double cost = c.calcCost();
			totalCost += cost;
			//c.printAttr();
			//System.out.println("cost"+(++i)+" :"+cost);
		}
		/*int totalNumCells = Cluster.original.numRows * Cluster.original.attributes.size();
		totalCost += (totalNumCells - numCells)*Cluster.averageCellCost;*/
		//System.out.println("total2 : "+totalCost);
		//2076808.422228242
		double nonCluster = ns.calcCost();//(new NonClusterSpace(this)).calcCost();
		//System.out.println("S: "+nonCluster);
		totalCost += nonCluster;
		//System.out.println("done");
		//2062018.7929820728
		return totalCost;
	}

	public void removeRow(Entry<Integer, Map<Column, Cell>> row) {
		Map<Column, Cell> nsRow = ns.cells.get(row.getKey());
		if(nsRow !=null){
			nsRow.putAll(row.getValue());
		}else{
			nsRow = new HashMap<Column, Cell>();
			nsRow.putAll(row.getValue());
			ns.cells.put(row.getKey(), nsRow);
			for(Cell cell : nsRow.values()){
				ns.addToAttributes(cell);
			}
		}
	}

	public void addRow(Entry<Integer, Map<Column, Cell>> row) {
		Map<Column, Cell> nsRow = ns.cells.get(row.getKey());
		for(Entry<Column, Cell> rowVals : row.getValue().entrySet()){
			nsRow.remove(rowVals.getKey());
			ns.removeFromAttributes(rowVals.getValue());
		}
	}

	public void addCells(List<Cell> notAddedToClus) {
		ns.removeCells(notAddedToClus);
		/*for(Cell cell : notAddedToClus){
			Map<Column, Cell> nsRow = ns.cells.get(cell.rowId);
			if(nsRow != null){
				nsRow.remove(new Column(cell, 1));
				if(nsRow.isEmpty()){
					ns.cells.remove(cell.rowId);
				}
				ns.removeFromAttributes(cell);
			}
		}*/
	}

	public void removeCells(List<Cell> addedToClus) {
		ns.addCells(addedToClus);
		/*for(Cell cell : addedToClus){
			Map<Column, Cell> nsRow = ns.cells.get(cell.rowId);
			if(nsRow != null){
				nsRow.put(new Column(cell,1), cell);
			}else{
				nsRow = new HashMap<Column, Cell>();
				nsRow.put(new Column(cell,1), cell);
				ns.cells.put(cell.rowId, nsRow);
			}
			ns.addToAttributes(cell);
		}*/
	}
}
