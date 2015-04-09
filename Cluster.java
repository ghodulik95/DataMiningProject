import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;



public class Cluster {
	public ArrayList<Column> attributes;
	//public ArrayList<Cell> rows;
	public Map<Integer, Map<Column, Cell>> cells;
	
	public Cluster(){
		//rows = new ArrayList<Cell>();
		attributes = new ArrayList<Column>();
		cells = new HashMap<Integer, Map<Column, Cell>>();
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
				Cluster ret = new Cluster();
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
											int vali = dist.getInt(columnName);
											col.addToProb(vali, cnt);
											break;
										case VARCHAR:
											String vals = dist.getString(columnName);
											col.addToProb(vals, cnt);
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
				return ret;
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
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
			return;
		}
	}

	public static Cluster makeBiggest(Cluster m, Cluster cluster, Column a) {
		
		switch(a.type){
			case INT:
				int mostCommon = -99999;
				int freq = Integer.MIN_VALUE;
				for(Entry<Integer, Integer> e : a.value_Int.entrySet()){
					if( e.getValue() > freq){
						freq = e.getValue();
						mostCommon = e.getKey();
					}
				}
				Cell cur = a.top;
				while(cur != null){
					if(cur.val_Int == mostCommon && m.cells.containsKey(cur.rowId) && cluster.cells.containsKey(cur.rowId)){
						
					}
					cur = cur.getDown();
				}
				break;
			case VARCHAR:
				break;
		}
	}
}
