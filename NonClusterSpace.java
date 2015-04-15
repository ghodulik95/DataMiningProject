import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class NonClusterSpace extends Cluster{

	public NonClusterSpace() {
		super();
	}
	
	public NonClusterSpace(Model m) {
		super();
		this.makeFromModel(m);
		this.setAttributes();
	}

	public void makeFromModel(Model m){
		int numInMod = 0;
		if(m.model.size() > 0){
			for(Integer rowId : Cluster.original.cells.keySet()){
				for(Entry<Column, Cell> e : Cluster.original.cells.get(rowId).entrySet()){
					boolean hasCell = false;
					for(Cluster inModel : m.model){
						if(inModel.cells.containsKey(rowId) && inModel.cells.get(rowId).containsKey(e.getKey())){
							hasCell = true;
							numInMod++;
							break;
						}
					}
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

}
