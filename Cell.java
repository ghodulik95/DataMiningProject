
public class Cell {
	public static enum Type {INT, VARCHAR};
	public final Type type;
	public final String val_String;
	public final int val_Int;
	private Cell right = null;
	private Cell left = null;
	private Cell up = null;
	private Cell down = null;
	public final int rowId;
	public final String colName;
	
	public Cell(Type t, String s, int r, String c){
		type = t;
		val_String = s;
		val_Int = -99999;
		rowId = r;
		colName = c;
	}
	
	public Cell(Type t, int val, int r, String c){
		type = t;
		val_String = null;
		val_Int = val;
		rowId = r;
		colName = c;
	}
	
	public boolean setRight(Cell r){
		if( r != null && r.rowId != this.rowId){
			return false;
		}
		right = r;
		return true;
	}
	
	public boolean setLeft(Cell l){
		if(l != null && l.rowId != this.rowId){
			return false;
		}
		left = l;
		return true;
	}
	
	public boolean setUp(Cell u){
		if(u != null && !u.colName.equals(this.colName)){
			return false;
		}
		up = u;
		return true;
	}
	
	public boolean setDown(Cell u){
		if(u != null && !u.colName.equals(this.colName)){
			return false;
		}
		down = u;
		return true;
	}
	
	public Cell getLeft(){
		return left;
	}
	
	public Cell getRight(){
		return right;
	}
	
	public Cell getUp(){
		return up;
	}
	
	public Cell getDown(){
		return down;
	}
	
	@Override
	public String toString(){
		switch(type){
			case INT:
				return ""+val_Int;
			case VARCHAR:
				return val_String;
		}
		return null;
	}
	
}
