import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class NonClusterSpace extends Cluster{
	
	private List<Entry<Column, Cell>> recentlyRemoved;
	private List<Cell> recentlyAddedCells;
	private List<Cell> recentlyRemovedCells;

	public NonClusterSpace() {
		super();
		addAllFromOriginal();
		this.setAttributes();
		recentlyRemoved = null;
		recentlyAddedCells = null;
		recentlyRemovedCells = null;
	}
	
	private void addAllFromOriginal() {
		for(Entry<Integer, Map<Column, Cell>> e : original.cells.entrySet()){
			Map<Column, Cell> row = new HashMap<Column, Cell>();
			row.putAll(e.getValue());
			this.cells.put(e.getKey(), row);
		}
	}

	public void addCluster(Cluster c){
		recentlyRemoved = new ArrayList<Entry<Column, Cell>>();
		for(Entry<Integer, Map<Column, Cell>> e : c.cells.entrySet()){
			Map<Column, Cell> row = this.cells.get(e.getKey());
			if(row != null){
				if(!row.isEmpty()){
					for(Entry<Column, Cell> inCluster : e.getValue().entrySet()){
						row.remove(inCluster.getKey());
						recentlyRemoved.add(inCluster);
						removeFromAttributes(inCluster.getValue());
					}
				}else{
					this.cells.remove(e.getKey());
				}
			}
		}
		//this.setAttributes();
	}
	

	public void removeCluster(){
		assert(recentlyRemoved != null);
		for(Entry<Column, Cell> r : recentlyRemoved){
			if(this.cells.containsKey(r.getValue().rowId)){
				Map<Column, Cell> row = this.cells.get(r.getValue().rowId);
				row.put(r.getKey(), r.getValue());
			}else{
				Map<Column, Cell> row = new HashMap<Column, Cell>();
				row.put(r.getKey(), r.getValue());
				this.cells.put(r.getValue().rowId, row);
			}
			addToAttributes(r.getValue());
		}
		recentlyRemoved = null;
	}
	
	

	public NonClusterSpace(Model m) {
		super();
		this.makeFromModel(m);
		this.setAttributes();
	}

	public void makeFromModel(Model m){
		Map<Integer, Map<Column, Cell>> cellsInModel = new HashMap<Integer, Map<Column, Cell>>();
		
		for(Cluster inModel: m.model){
			for(Integer rowId : inModel.cells.keySet()){
				if(cellsInModel.containsKey(rowId)){
					cellsInModel.get(rowId).putAll(inModel.cells.get(rowId));
				}else{
					Map<Column, Cell> newRow = new HashMap<Column, Cell>();
					newRow.putAll(inModel.cells.get(rowId));
					cellsInModel.put(rowId, newRow);
				}
			}
		}
		
		int numInMod = 0;
		if(m.model.size() > 0){
			for(Integer rowId : Cluster.original.cells.keySet()){
				for(Entry<Column, Cell> e : Cluster.original.cells.get(rowId).entrySet()){
					boolean hasCell = cellsInModel.containsKey(rowId) && cellsInModel.get(rowId).containsKey(e.getKey());
					/*for(Cluster inModel : m.model){
						if(inModel.cells.containsKey(rowId) && inModel.cells.get(rowId).containsKey(e.getKey())){
							hasCell = true;
							numInMod++;
							break;
						}
					}*/
					if(!hasCell){
						if(this.cells.containsKey(rowId) && !this.cells.get(rowId).containsKey(e.getKey())){
							this.cells.get(rowId).put(e.getKey(), e.getValue());
						}else if(!this.cells.containsKey(rowId)){
							Map<Column, Cell> row = new HashMap<Column, Cell>();
							row.put(e.getKey(), e.getValue());
							this.cells.put(rowId, row);
						}
					}
				}
			}
		}else{
			for(Integer rowId : Cluster.original.cells.keySet()){
				for(Entry<Column, Cell> e : Cluster.original.cells.get(rowId).entrySet()){
					if(this.cells.containsKey(rowId) && !this.cells.get(rowId).containsKey(e.getKey())){
						this.cells.get(rowId).put(e.getKey(), e.getValue());
					}else if(!this.cells.containsKey(rowId)){
						Map<Column, Cell> row = new HashMap<Column, Cell>();
						row.put(e.getKey(), e.getValue());
						this.cells.put(rowId, row);
					}
				}
			}
		}
		this.setAttributes();
		/*int numCellsIn = 0;
		for(Map<Column, Cell> r : cells.values()){
			numCellsIn += r.size();
		}
		System.out.println(numCellsIn+numInMod);*/
	}
	
	
	@Override
	public double calcCost(){
		double ret = 0.0;
		int numParams = 0;
		for(Column c : attributes){
			double codingCost = 0.0;
			codingCost += c.calcEntropy();
			//System.out.println(c.attrName+" "+c.calcEntropy()+" #"+c.numRows);
			if(c.type == Cell.Type.INT){
				numParams += c.value_Int.size();
			}else{
				numParams += c.value_String.size();
			}

			codingCost = codingCost*c.numRows;
			
			//double probInThis = (1.0*c.numRows)/originalNumRows;
			//double objAssignmentCost = 0.0;
			//if(probInThis > 0.0)
			//	objAssignmentCost += -originalNumRows*probInThis*Math.log(probInThis)/Math.log(2);
			//if(probInThis < 1.0)
			//	objAssignmentCost += -originalNumRows*(1 - probInThis)*Math.log(1 - probInThis)/Math.log(2);

			double probabilities = 0.5*numParams*Math.log(c.numRows)/Math.log(2);
			
			ret += codingCost + probabilities;// + objAssignmentCost;
			//System.out.println("\tCC "+codingCost);
			//System.out.println("\tOB "+objAssignmentCost);
		}
		//double probabilities = 0.5*numParams*Math.log(original.numRows)/Math.log(2);
		//ret += probabilities;

		//System.out.println("\tPR "+probabilities);
		/*for(Map<Column, Cell> row : cells.values()){
			double probInThis = (1.0*row.size())/originalNumAttr;
			double attrAssignmentCost = 0.0;
			if(probInThis > 0.0)
				attrAssignmentCost += -originalNumAttr*probInThis*Math.log(probInThis)/Math.log(2);
			if(probInThis < 1.0)
				attrAssignmentCost += -originalNumAttr*(1 - probInThis)*Math.log(1 - probInThis)/Math.log(2);
			//probabilities = 0.5*row.size()*Math.log(row.size())/Math.log(2);
			ret += attrAssignmentCost;// + probabilities;
			//System.out.println("\tAT "+probInThis);
		}*/
		return ret;
		
		
	}

	public void resetRecentlyAddedCells() {
		recentlyAddedCells = new ArrayList<Cell>();
	}

	@Override
	public void addCell(Cell value) {
		recentlyAddedCells.add(value);
		if(!attributes.contains(new Column(value, 0))){
			attributes.add(new Column(value, 0));
		}
		super.addCell(value);
	}
	
	@Override
	public void removeCell(Cell cell){
		recentlyRemovedCells.add(cell);
		super.removeCell(cell);
	}

	public void removeCellsBack() {
		super.removeCells(recentlyAddedCells);
		recentlyAddedCells = null;
	}

	public void resetRecentlyRemovedCells() {
		recentlyRemovedCells = new ArrayList<Cell>();
	}
	
	public void addRemovedBack(){
		super.addCells(recentlyRemovedCells);
		recentlyRemovedCells = null;
	}

}
