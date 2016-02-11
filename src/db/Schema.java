package db;

/**
 * Schema can be viewed as a collection of columns. Each tuple has a schema.
 * Schema is IMMUTABLE.
 * Each schema does not necessarily correpsond to a table. 
 * Eg. A tuple after join/projection might not come from ONE existing table. 
 * @author Alvin Zhu
 *
 */
public abstract class Schema {
	
	protected CustomColumn[] columns;	
	
	public Schema(CustomColumn[] columns){
		this.columns = columns;
	}
	
	/* Returns the index of the specified Column in this schema. */
	public abstract int getColumnIndex(CustomColumn col) throws IllegalArgumentException;
	
	/**
	 * Return whether the schema contains a column with the specific name
	 * @param colName
	 * @return whether the column is contained in here
	 */
	public abstract boolean hasColumn(CustomColumn col);

	/**
	 * @param col
	 * @return the type corresponding to the column
	 * @throws IllegalArgumentException If the column isn't in the schema
	 */
	public DATA_TYPE getTypeForColumn(CustomColumn col){
		try{
			return this.columns[this.getColumnIndex(col)].getType();
		}catch(IllegalArgumentException ill){
			return DATA_TYPE.NULL;
		}
	}
	
	/**
	 * Checks whether the data is a valid data for the schema
	 * TODO since we don't have type system yet, we just check 
	 * 		1. whether the data array has the same length as the column array
	 * 		2. whether the data contains all Integers
	 * @param data 	This contains the data to be checked	
	 * @return		Whether the data is valid
	 */
	public boolean isValidData(Object[] data){
		for (int i = 0 ; i < data.length ; i++){

			if (!(data[i] instanceof Integer)){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 *  Return the total number of columns in this schema. 
	 */
	public int getNumberOfColumns(){
		return columns.length;
	}
	
	/**
	 * TODO this is not too good, since others can change the columns
	 * TODO Can we ensure this is ordered? Necessary for Sort Operator
	 * @return The current columns information
	 */
	public CustomColumn[] getColumns(){
		return this.columns;
	}
	
	@Override
	public String toString(){
		String ret = "";
		for(int i = 0 ; i < this.columns.length; i++){
			ret += this.columns[i] +" ";
		}
		return ret;
	}
	
	@Override
	public boolean equals(Object other){
		Schema ot = (Schema) other;
		if( ot.getNumberOfColumns() != getNumberOfColumns())
			return false;
		CustomColumn[] me = this.getColumns();
		CustomColumn[] you = ot.getColumns();
		for( int i = 0; i < getNumberOfColumns(); i++){
			if( ! me[i].equals(you[i]))
				return false;
		}
		return true;
	}
	
	/**
	 * CLone the schema
	 */
	public abstract Schema clone();
	
	/**
	 * TODO
	 * @param cols
	 * @return
	 */
	public abstract Schema subSchema(CustomColumn[] cols);
}
