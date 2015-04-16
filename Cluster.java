import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class Cluster {
	public ArrayList<Column> attributes;
	//public ArrayList<Cell> rows;
	public Map<Integer, Map<Column, Cell>> cells;
	public int numRows;
	public static int originalNumRows = -1;
	public static Cluster original;
	public static double averageCellCost = -1;
	public static double originalCost;
	public static double originalNumAttr;
	
	public Cluster(int n){
		//rows = new ArrayList<Cell>();
		attributes = new ArrayList<Column>();
		cells = new HashMap<Integer, Map<Column, Cell>>();
		numRows = n;
	}
	
	public Cluster(){
		attributes = new ArrayList<Column>();
		cells = new HashMap<Integer, Map<Column, Cell>>();
		numRows = 0;
	}
	
	public static Cluster clusterFromQuery(String select, String from, String where){
		Communicator com = new Communicator();
		if(!com.connect()){
			return null;
		}
		ResultSet rs = com.query(select + " "+ from + " " + where);
		try{
			if(rs == null){
				System.out.println("Error with query");
				return null;
			}else if(!rs.next()){
				System.out.println("No results from query");
				return null;
			}else{
				rs.last();
				int total = rs.getRow();
				rs.first();
				Cluster ret = new Cluster(total);
				originalNumRows = total;
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				Cell left = null;
				Cell up = null;
				Cell firstUp = null;
				boolean first = true;
				do{
					int rowId = rs.getInt(1);
					Map<Column, Cell> currentRow = new HashMap<Column, Cell>();
					ret.cells.put(rowId, currentRow);
					Cell.Type type = null;
					String columnName = null;
					String colValString = null;
					int colValInt = -99999;
					for (int i = 2; i <= columnsNumber; i++) {
						int t = rsmd.getColumnType(i);
						columnName = rsmd.getColumnName(i);
						//System.out.println(columnName+" -- "+t);
						Cell cur;
						if(t == 12){
							type = Cell.Type.VARCHAR;
							//System.out.println(columnName);
							colValString = rs.getString(i);
							cur = new Cell(type, colValString, rowId, columnName);
						}else if(t == 4){
							type = Cell.Type.INT;
							colValInt = rs.getInt(i);
							cur = new Cell(type, colValInt, rowId, columnName);
						}else{
							System.out.println("NO TYPE");
							return null;
						}
						cur.setLeft(left);
						if(left!=null){
							left.setRight(cur);
						}
						left = cur;
						
						cur.setUp(up);
						if(up != null){
							up.setDown(cur);
							up = up.getRight();
						}
						if(i == 2){
							//ret.rows.add(cur);
							firstUp = cur;
						}
						if(first){
							Column col = new Column(cur, total);
							ResultSet dist = com.query("Select "+columnName+", count(*) as cnt  "+from+ " "+where+" GROUP BY "+columnName);
							if(dist == null){
								System.out.println("Error with query2");
								return null;
							}else if(!dist.next()){
								System.out.println("No results from query2");
								return null;
							}else{
								do{
									int cnt = dist.getInt("cnt");
									switch(type){
										case INT:
											Integer vali = dist.getInt(columnName);
											if(!dist.wasNull()){
												col.addToProb(vali, cnt);
											}else{
												col.addNull(type, cnt);
											}
											break;
										case VARCHAR:
											String vals = dist.getString(columnName);
											if(!dist.wasNull()){
												col.addToProb(vals, cnt);
											}else{
												col.addNull(type, cnt);
											}
											break;
									}
								}while(dist.next());
								dist.close();
								currentRow.put(col, cur);
								ret.attributes.add(col);
							}
						}else{
							currentRow.put(ret.attributes.get(i-2), cur);
						}
						
						if(i == columnsNumber){
							first = false;
							up = firstUp;
						}
					}
				}while(rs.next());
				rs.close();
				original = ret;
				setAverageCellSize();
				originalNumAttr = original.attributes.size();
				/*for(Column col : ret.attributes){

					int count = 0;
					if(col.type == Cell.Type.INT){
						for(Integer in : col.value_Int.values()){
							count += in;
						}
					}else{
						for(Integer in : col.value_String.values()){
							count += in;
						}
					}
					if(count != 4952){
						System.out.println(col.attrName);
					}
				}*/
				return ret;
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}

	private static void setAverageCellSize() {
		averageCellCost = 0.0;
		double totalCost = 0.0;
		for(Column a : original.attributes){
			double ent = a.calcEntropy();
			totalCost += ent;
			averageCellCost += ent/original.attributes.size();
		}
		originalCost = totalCost*original.attributes.size();
	}

	@Override
	public String toString(){
		System.out.println("Printing");
		String ret = "";
		for(int row : cells.keySet()){
			boolean first = true;
			for(Column col : cells.get(row).keySet()){
				if(!first)
					ret += ", ";
				ret += cells.get(row).get(col).toString();
				first = false;
			}
			ret += "\n";
			System.out.print(ret);
			ret = "";
		}/*
		Cell cur = rows.get(0);
		Cell below = cur.getDown();
		assert(cur != null);
		int cnt = 0;
		printLoop:
		while(true){
			boolean first = true;
			while(cur != null){
				if(!first)
					ret += ", ";
				ret += cur.toString();
				cur = cur.getRight();
				first = false;
			}
			if(below != null){
				below = below.getDown();
				cur = below;
				ret += "\n";
				cnt++;
				return null;
			}else{
				break printLoop;
			}
		}*/
		return ret;
	}
	
	public void printAttr(){
		for(Column c : attributes){
			System.out.println(c);
		}
	}
	
	public Cluster addAttr(Cluster m, Column a, List<Integer> rowIds){
		Cluster ret = new Cluster(rowIds.size());
		for(Integer rowId : rowIds){
			Map<Column, Cell> cell = new HashMap<Column, Cell>();
			for(Column c : attributes){
				cell.put(c, cells.get(rowId).get(c));
			}
			cell.put(a, m.cells.get(rowId).get(a));
			ret.cells.put(rowId, cell);
		}
		ret.setAttributes();
		return ret;
	}

	protected void setAttributes() {
		attributes = new ArrayList<Column>();
		for(Integer rowId : cells.keySet()){
			for(Entry<Column, Cell> e : cells.get(rowId).entrySet()){
				Column col = e.getKey();
				Cell cur = e.getValue();
				if(!attributes.contains(col)){
					attributes.add(new Column(cur, 0));
				}
				int ind = -1;
				if( (ind = attributes.indexOf(col)) != -1){
					Column a = attributes.get(ind);
					a.numRows++;
					switch(cur.type){
						case INT:
							if(a.value_Int.containsKey(cur.val_Int)){
								a.value_Int.put(cur.val_Int, a.value_Int.get(cur.val_Int) + 1);
							}else{
								a.value_Int.put(cur.val_Int, 1);
							}
							break;
						case VARCHAR:
							if(a.value_String.containsKey(cur.val_String)){
								a.value_String.put(cur.val_String, a.value_String.get(cur.val_String) + 1);
							}else{
								a.value_String.put(cur.val_String, 1);
							}
							break;
					}
				}else{
					System.err.println("No column");
				}
			}
		}
	}
	
	public static Collection<Integer> findValueDistribution(Cluster m, Column a, Set<Integer> potentialRows){
		Cell cur;
		switch(a.type){
			case INT:
				Map<Integer, Integer> valDistInt = new HashMap<Integer, Integer>();
				for(Integer rowId : potentialRows){
					cur = m.cells.get(rowId).get(a);
					if(valDistInt.containsKey(cur.val_Int)){
						valDistInt.put(cur.val_Int, valDistInt.get(cur.val_Int) + 1);
					}else{
						valDistInt.put(cur.val_Int, 1);
					}
				}
				return valDistInt.values();
			case VARCHAR:
				Map<String, Integer> valDistStr = new HashMap<String, Integer>();
				for(Integer rowId : potentialRows){
					cur = m.cells.get(rowId).get(a);
					if(valDistStr.containsKey(cur.val_String)){
						valDistStr.put(cur.val_String, valDistStr.get(cur.val_String) + 1);
					}else{
						valDistStr.put(cur.val_String, 1);
					}
				}
				return valDistStr.values();
			
		}
		return null;
	}
	
	public static List<Integer> findMostCommonValueRows(Cluster m, Column a, Set<Integer> potentialRows){
		int freq = Integer.MIN_VALUE;
		Cell cur;
		List<Integer> rowIds = new ArrayList<Integer>();
		switch(a.type){
			case INT:
				Integer mostCommon = -99999;
				for(Entry<Integer, Integer> e : a.value_Int.entrySet()){
					if( e.getValue() > freq){
						freq = e.getValue();
						mostCommon = e.getKey();
					}
				}
				for(Integer rowId : potentialRows){
					cur = m.cells.get(rowId).get(a);
					if(cur.val_Int == mostCommon){
						rowIds.add(rowId);
					}
				}
				break;
			case VARCHAR:
				String mostCommonS = "";
				for(Entry<String, Integer> e : a.value_String.entrySet()){
					if( e.getValue() > freq){
						freq = e.getValue();
						mostCommonS = e.getKey();
					}
				}
				for(Integer rowId : potentialRows){
					cur = m.cells.get(rowId).get(a);
					if((cur.val_String == null && mostCommonS == null) || (cur.val_String != null && cur.val_String.equals(mostCommonS))){
						rowIds.add(rowId);
					}
				}
				break;
			
		}
		return rowIds;
	}

	public static Cluster makeBiggest(Cluster m, Cluster cluster, Column a) {
		List<Integer> rowIds = findMostCommonValueRows(m, a, cluster.cells.keySet());
		//System.out.println("Row size "+rowIds.size());
		return cluster.addAttr(m, a, rowIds);
	}
	
	public double calcCost(){
		double codingCost = 0.0;
		int numParams = 0;
		for(Column c : attributes){
			codingCost += c.calcEntropy();
			if(c.type == Cell.Type.INT){
				numParams += c.value_Int.size();
			}else{
				numParams += c.value_String.size();
			}
		}
		codingCost = codingCost*numRows;
		double probInThis = (1.0*numRows)/originalNumRows;
		double objAssignmentCost = 0.0;
		if(probInThis > 0.0)
			objAssignmentCost += -originalNumRows*probInThis*Math.log(probInThis)/Math.log(2);
		if(probInThis < 1.0)
			objAssignmentCost += -originalNumRows*(1 - probInThis)*Math.log(1 - probInThis)/Math.log(2);
		
		double attrAssignmentCost = 0.0;
		probInThis = (1.0*attributes.size())/originalNumAttr;
		if(probInThis > 0.0)
			attrAssignmentCost += -originalNumAttr*probInThis*Math.log(probInThis)/Math.log(2);
		if(probInThis < 1.0)
			attrAssignmentCost += -originalNumAttr*(1 - probInThis)*Math.log(1 - probInThis)/Math.log(2);

		double probabilities = 0.5*numParams*Math.log(numRows)/Math.log(2);
		/*System.out.println("CC "+codingCost);
		System.out.println("AT "+attrAssignmentCost);
		System.out.println("PR "+probabilities);
		System.out.println("OB "+objAssignmentCost);*/
		return codingCost + objAssignmentCost + attrAssignmentCost + probabilities;
	}
	
	public Cluster getComplement(Cluster bound){
		if(attributes.size() == original.attributes.size()){
			return null;
		}
		Cluster ret = new Cluster(this.numRows);
		ArrayList<Column> cols = new ArrayList<Column>();
		cols.addAll(bound.attributes);
		for(Column a : attributes){
			cols.remove(a);
		}
		for(Integer rowId : cells.keySet()){
			Map<Column, Cell> row = new HashMap<Column, Cell>();
			for(Column a : cols){
				row.put(a, original.cells.get(rowId).get(a));
			}
			ret.cells.put(rowId, row);
		}
		ret.setAttributes();
		//ret.printAttr();
		return ret;
	}

	public static Cluster cutPureAttr(Cluster m, Column a) {
		List<Integer> rowIds = findMostCommonValueRows(m, a, m.cells.keySet());
		Cluster ret = new Cluster(rowIds.size());
		Map<Column, Cell> r = new HashMap<Column, Cell>();
		for(Integer row : rowIds){
			Cell cur = m.cells.get(row).get(a);
			/*if(!ret.attributes.contains(a)){
				ret.attributes.add(new Column( cur, rowIds.size()));
			}else{
				Column attr = ret.attributes.get(ret.attributes.indexOf(a));
				switch(cur.type){
					case INT:
						if(attr.value_Int.containsKey(cur.val_Int)){
							attr.value_Int.put(cur.val_Int, attr.value_Int.get(cur.val_Int) + 1);
						}else{
							attr.value_Int.put(cur.val_Int, 1);
						}
						break;
					case VARCHAR:
						if(attr.value_String.containsKey(cur.val_String)){
							attr.value_String.put(cur.val_String, attr.value_String.get(cur.val_String) + 1);
						}else{
							attr.value_String.put(cur.val_String, 1);
						}
						break;
				}
			}*/
			r.put(a, cur);
			ret.cells.put(row, r);
			r = new HashMap<Column, Cell>();
		}
		ret.setAttributes();
		return ret;
	}
	
	public void printCells(){
		for(Integer rowId : cells.keySet()){
			if(cells.get(rowId).size() < 3){
				System.err.println("Not 3");
				return;
			}
			System.out.print("Row "+rowId+": ");
			for(Entry<Column, Cell> e : cells.get(rowId).entrySet()){
				System.out.print("("+e.getKey()+", "+e.getValue()+")");
			}
			System.out.println();
		}
	}
}
