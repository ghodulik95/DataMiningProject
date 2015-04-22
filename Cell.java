import java.io.Serializable;


public class Cell implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum Type {INT, VARCHAR};
	public final Type type;
	public final String val_String;
	public final int val_Int;
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
