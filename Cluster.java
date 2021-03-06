import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class Cluster implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Column> attributes;
	public Map<Integer, Map<Column, Cell>> cells;
	public int numRows;
	public static int originalNumRows = -1;
	public static Cluster original;
	public static double averageCellCost = -1;
	public static double originalCost;
	public static double originalNumAttr;
	
	public Cluster(int n){
		attributes = new ArrayList<Column>();
		cells = new HashMap<Integer, Map<Column, Cell>>();
		numRows = n;
	}
	
	public Cluster(){
		attributes = new ArrayList<Column>();
		cells = new HashMap<Integer, Map<Column, Cell>>();
		numRows = 0;
	}
	
	public static Cluster clusterFromQuery(String select, String from, String where, 
			String uname, String pass, String dbname){
		Communicator com = new Communicator(uname, pass, dbname);
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
						Cell cur;
						if(t == 12){
							type = Cell.Type.VARCHAR;
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
						if(first){
							Column col = new Column(cur, total);
							ResultSet dist = com.query("Select "+columnName+", count(*) as cnt  "+from+ "  GROUP BY "+columnName +" ");
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
						}
					}
				}while(rs.next());
				rs.close();
				original = ret;
				setAverageCellSize();
				originalNumAttr = original.attributes.size();
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
		}
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
		return ret;
	}

	public static Cluster cutPureAttr(Cluster m, Column a) {
		List<Integer> rowIds = findMostCommonValueRows(m, a, m.cells.keySet());
		Cluster ret = new Cluster(rowIds.size());
		Map<Column, Cell> r = new HashMap<Column, Cell>();
		for(Integer row : rowIds){
			Cell cur = m.cells.get(row).get(a);
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

	public List<Cell> addRow(Entry<Integer, Map<Column, Cell>> row) {
		for(Column col : attributes){
			Cell inRow = row.getValue().get(col);
			switch(col.type){
			case INT:
				if(null == col.value_Int.get(inRow.val_Int)){
					return null;
				}
				break;
			case VARCHAR:
				if(null == col.value_String.get(inRow.val_String)){
					return null;
				}
				break;
			}
		}
		Map<Column,Cell> newRow = new HashMap<Column,Cell>();
		cells.put(row.getKey(), newRow);
		List<Cell> toReturn = new ArrayList<Cell>();
		for(Entry<Column,Cell> e : row.getValue().entrySet()){
			if(attributes.contains(e.getKey())){
				newRow.put(e.getKey(), e.getValue());
				addToAttributes(e.getValue());
				toReturn.add(e.getValue());
			}
		}
		numRows++;
		return toReturn;
	}

	public void removeRow(Integer key) {
		for(Cell cell : cells.get(key).values()){
			removeFromAttributes(cell);
		}
		numRows--;
		cells.remove(key);
	}
	
	
	void addToAttributes(Cell cell) {
		for(Column c : this.attributes){
			if(c.attrName.equals(cell.colName)){
				c.addCell(cell);
				return;
			}
		}
		this.attributes.add(new Column(cell, 1));
	}
	

	void removeFromAttributes(Cell cell) {
		for(Column c : this.attributes){
			if(c.attrName.equals(cell.colName)){
				c.removeCell(cell);
				if(c.numRows == 0){
					this.attributes.remove(c);
				}
				return;
			}
		}
	}

	public List<Cell> addColumn(Column a) {
		List<Cell> toReturn = new ArrayList<Cell>();
		boolean first = true;
		for(Entry<Integer, Map<Column, Cell>> e: this.cells.entrySet()){
			Cell cell = original.cells.get(e.getKey()).get(a);
			if(first){
				first = false;
				attributes.add(new Column(cell, 0));
			}
			e.getValue().put(a, cell);
			toReturn.add(cell);
			addToAttributes(cell);
		}
		
		return toReturn;
	}
	
	public void addCells(List<Cell> addedToClus) {
		for(Cell cell : addedToClus){
			this.addCell(cell);
		}
	}
	
	public void addCell(Cell cell){
		Map<Column, Cell> nsRow = cells.get(cell.rowId);
		if(nsRow != null){
			nsRow.put(new Column(cell,1), cell);
		}else{
			nsRow = new HashMap<Column, Cell>();
			nsRow.put(new Column(cell,1), cell);
			cells.put(cell.rowId, nsRow);
		}
		addToAttributes(cell);
	}
	
	public void removeCells(List<Cell> notAddedToClus) {
		
		for(Cell cell : notAddedToClus){
			this.removeCell(cell);
		}
	}
	
	public void removeCell(Cell cell){
		Map<Column, Cell> nsRow = cells.get(cell.rowId);
		if(nsRow != null){
			nsRow.remove(new Column(cell, 1));
			if(nsRow.isEmpty()){
				cells.remove(cell.rowId);
			}
			removeFromAttributes(cell);
		}
	}

}
