
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
	
	public void makeNS(){
		ns.makeFromModel(this);
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
		for(Cluster c : model){
			double cost = c.calcCost();
			totalCost += cost;
		}
		double nonCluster = ns.calcCost();
		totalCost += nonCluster;
		return totalCost;
	}

	public void removeRow(Entry<Integer, Map<Column, Cell>> row) {
		ns.resetRecentlyAddedCells();
		for(Entry<Column, Cell> rowVal : row.getValue().entrySet()){
			if(!containedByCluster(rowVal)){
				ns.addCellNS(rowVal.getValue());
			}
		}
	}

	private boolean containedByCluster(Entry<Column, Cell> rowVal) {
		for(Cluster c : model){
			if(c.cells.containsKey(rowVal.getValue().rowId) && c.cells.get(rowVal.getValue().rowId).containsKey(rowVal.getKey())){
				return true;
			}
		}
		return false;
	}

	public void addCellsBack(Entry<Integer, Map<Column, Cell>> row) {
		ns.removeCellsBack();
	}

	public void addCells(List<Cell> addedToClus) {
		ns.resetRecentlyRemovedCells();
		for(Cell cell : addedToClus){
			if(ns.cells.containsKey(cell.rowId) && ns.cells.get(cell.rowId).containsKey(new Column(cell, 0))){
				ns.removeCellNS(cell);
			}
		}
	}

	public void removeCellsBack() {
		ns.addRemovedBack();
	}

}
