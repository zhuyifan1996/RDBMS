package db;

import java.util.HashMap;

import main.Logger;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * This class is an implementation of the schema using dictionary
 * we believe there will be more efficient way to implement this class
 * @author Guandao Yang
 *
 */
public class SchemaWithDictionary extends Schema {

	private HashMap<CustomColumn, Integer> columnIndexes;
	
	/**
	 * Create a schema for columns in [columns] and adding alias [alias] for 
	 * all columns coming form table named [table]
	 * @param columns
	 * @param alias
	 * @param table
	 */
	public SchemaWithDictionary(CustomColumn[] columns, String alias, String table) {
		super(columns);
		
		// initialize the indexing hash map
		this.columnIndexes = new HashMap<CustomColumn, Integer>();
		CustomColumn[] col = new CustomColumn[columns.length];
		for (int i = 0; i < columns.length; i++){
			col[i] = columns[i].clone();
			
			// replace alias
			if (col[i].getTable().getName().equals(table)){
				col[i].getTable().setAlias( alias );
			}
			this.columnIndexes.put(col[i], (new Integer(i)));
		}
		
		this.columns = col;
	}
	
	/**
	 * Initiliaze a bunch of columns and set all alias to null
	 * @param columns
	 */
	public SchemaWithDictionary(CustomColumn[] columns) {
		super(columns);
		
		// initialize the indexing hash map
		this.columnIndexes = new HashMap<CustomColumn, Integer>();
		for (int i = 0; i < columns.length; i++){
			this.columnIndexes.put(columns[i], (new Integer(i)));
		}
	}

	@Override
	public int getColumnIndex(CustomColumn col) throws IllegalArgumentException{
		if (!hasColumn(col)){
			throw new IllegalArgumentException("Column "+ col +" doesn't exists in schema " + this);
		}

		return this.columnIndexes.get(col);
	}

	@Override
	public boolean hasColumn(CustomColumn col) {
		return this.columnIndexes.containsKey(col);
	}
	
	/**
	 * @return A copy of all the columns in the schema
	 */
	public CustomColumn[] getColumns(){
		CustomColumn[] ret = new CustomColumn[this.columns.length];
		int i = 0;
		for (CustomColumn c : this.columns){
			ret[i] = c; 
			i++;
		}
		return ret;
		
	}

	/** Generates a subschema of this schema. Retains aliases from this schema.
	 * [pre]: ALL columns in cols must be already in this schema!! Otherwise will throw exception.
	 * @param cols: The cols that should be retained. Comparison is done with customized equals of CustomizedColumn
	 * @return A new child schema.
	 */
	@Override
	public Schema subSchema(CustomColumn[] cols) throws IllegalArgumentException {
		CustomColumn[] newCols = new CustomColumn[cols.length]; 
		CustomColumn c;
		for (int i =0;i<cols.length;i++){
			c = cols[i];
			for (CustomColumn cc: this.getColumns()){
				if (c.equals(cc)){
					newCols[i]=cc;
					break;
				}
			}
			if (newCols[i]==null) throw new IllegalArgumentException();
		}
		return new SchemaWithDictionary(newCols);
	}
	
	@Override
	public Schema clone() {
		// TODO
		Logger.warnln("Cloning with unimplemented method");
		return null;
	}
	
	/**
	 * Create a dummy schema with specific length. 
	 * No any information about column and table, won't be able to
	 * perform any query with this schema
	 * @param len
	 */	
	public static Schema createDummySchemaForLength(int len){
		CustomColumn[] cols = new CustomColumn[len];
		Table dummyTable = new Table();
		dummyTable.setName("DummyT");
		for (int i = 0 ; i < len; i++){
			Column c = new Column();
			c.setColumnName("f"+i);
			c.setTable(dummyTable);
			cols[i] = new CustomColumn(c);
		}
		return new SchemaWithDictionary(cols);
	}
	

}
