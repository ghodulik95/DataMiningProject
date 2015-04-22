import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;





public class Column implements Serializable{
	//public Cell top;
	public final Cell.Type type;
	public Map<String, Integer> value_String;
	public Map<Integer, Integer> value_Int;
	public final String attrName;
	public int numRows;
	
	public Column(Cell top, int numRows){
		//this.top = top;
		this.type = top.type;
		this.attrName = top.colName;
		this.numRows = numRows;
		switch(type){
			case INT:
				value_Int = new HashMap<Integer, Integer>();
				//value_Int.put(top.val_Int, 1);
				value_String = null;
				break;
			case VARCHAR:
				value_String = new HashMap<String, Integer>();
				//value_String.put(top.val_String, 1);
				value_Int = null;
				break;
		}
	}
	
	public void addToProb(String s, int count){
		value_String.put(s, count);
	}
	
	public void addToProb(Integer val, int count){
		value_Int.put(val, count);
	}
	
	public double calcEntropy(){
		Collection<Integer> vals = new ArrayList<Integer>();
		switch(type){
			case INT:
				vals.addAll(value_Int.values());
				break;
			case VARCHAR:
				vals.addAll(value_String.values());
				break;
		}
		Double total = 0.0;
		for(Integer count : vals){
			double prob = (1.0*count)/numRows;
			total += prob*Math.log(prob)/Math.log(2);
		}
		return -total;
	}
	
	public static double calcEntropy(Collection<Integer> vals, int totalNumRows){
		Double total = 0.0;
		for(Integer count : vals){
			double prob = (1.0*count)/totalNumRows;
			total += prob*Math.log(prob)/Math.log(2);
		}
		return -total;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Column){
			return ((Column)o).attrName.equals(attrName);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return attrName.hashCode();
	}
	
	@Override
	public String toString(){
		String ret = "";
		ret += attrName+": ";
		ret += type+" -- ";
		ret += "{";
		boolean first = true;
		switch(type){
			case INT:
				for(Entry<Integer, Integer> e : value_Int.entrySet()){
					if(!first)
						ret += ", ";
					ret += e.getKey()+": "+e.getValue();
					first = false;
				}
				break;
			case VARCHAR:
				for(Entry<String, Integer> e : value_String.entrySet()){
					if(!first)
						ret += ", ";
					ret += e.getKey()+": "+e.getValue();
					first = false;
				}
				break;
		}
		ret += "} ";
		ret += calcEntropy()+"";
		return ret;
	}

	public void addNull(Cell.Type t, int cnt) {
		switch(type){
			case INT:
				value_Int.put(null, cnt);
				break;
			case VARCHAR:
				value_String.put(null, cnt);
				break;
		}
	}

	public void removeCell(Cell cell) {
		Integer num;
		switch(type){
		case INT:
			num = value_Int.get(cell.val_Int);
			if(num != null && num > 1){
				value_Int.put(cell.val_Int, num - 1);
			}else if(num != null){
				value_Int.remove(cell.val_Int);
			}else{
				//System.err.println("Uh OHHHHH!");
			}
			numRows--;
			break;
		case VARCHAR:
			num = value_String.get(cell.val_String);
			if(num != null && num > 1){
				value_String.put(cell.val_String, num - 1);
			}else if(num != null){
				value_String.remove(cell.val_String);
			}else{
				//System.err.println("Uh OHHHHH!");
			}
			numRows--;
			break;
		}
	}

	public void addCell(Cell cell) {
		Integer num;
		switch(type){
		case INT:
			num = value_Int.get(cell.val_Int);
			if(num == null){
				value_Int.put(cell.val_Int, 1);
			}else{
				value_Int.put(cell.val_Int, num + 1);
			}
			numRows++;
			break;
		case VARCHAR:
			num = value_String.get(cell.val_String);
			if(num == null){
				value_String.put(cell.val_String, 1);
			}else{
				value_String.put(cell.val_String, num + 1);
			}
			numRows++;
			break;
		}
	}

}
