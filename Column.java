import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;




public class Column{
	public Cell top;
	public final Cell.Type type;
	public Map<String, Double> probability_String;
	public Map<Integer, Double> probability_Int;
	public final String attrName;
	
	public Column(Cell top){
		this.top = top;
		this.type = top.type;
		this.attrName = top.colName;
		switch(type){
			case INT:
				probability_Int = new HashMap<Integer, Double>();
				probability_String = null;
				break;
			case VARCHAR:
				probability_String = new HashMap<String, Double>();
				probability_Int = null;
				break;
		}
	}
	
	public void addToProb(String s, int count, int total){
		probability_String.put(s, (1.0*count)/total);
	}
	
	public void addToProb(int val, int count, int total){
		probability_Int.put(val, (1.0*count)/total);
	}
	
	public double calcEntropy(){
		Collection<Double> vals = new ArrayList<Double>();
		switch(type){
			case INT:
				vals.addAll(probability_Int.values());
				break;
			case VARCHAR:
				vals.addAll(probability_String.values());
				break;
		}
		Double total = 0.0;
		for(Double prob : vals){
			total += prob*Math.log(prob)/Math.log(2);
		}
		return -total;
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
				for(Entry<Integer, Double> e : probability_Int.entrySet()){
					if(!first)
						ret += ", ";
					ret += e.getKey()+": "+e.getValue();
					first = false;
				}
				break;
			case VARCHAR:
				for(Entry<String, Double> e : probability_String.entrySet()){
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
}
