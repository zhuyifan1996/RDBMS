package db;

import java.util.Arrays;

import db.Schema;
import main.Logger;

/**
 * The tuple class represents a row in a relational table.
 * @author Guandao Yang
 */
public class Tuple {

	private Schema schema;
	private Object[] data;

	/**
	 * Construct a tuple 
	 * TODO where do we create the indexing?
	 * @param scm
	 * @param data
	 * @throws IllegalArgumentException
	 */
	public Tuple(Schema scm, Object[] data) throws IllegalArgumentException{
		if( scm != null ){
			if (!scm.isValidData(data)){
				throw new IllegalArgumentException("The data doesn't conform to the schema");
			}
		}else{
			Logger.warnln("IGNORING LACK OF SCHEMA");
		}
		this.schema = scm;
		this.data = data;
	}

	/**
	 * @return The schema of this tuple.
	 */
	public Schema getSchema(){
		return this.schema;
	}

	/**
	 * @return The data of this tuple.
	 */
	public Object[] getData(){
		return this.data;
	}

	/**
	 * @param c The column we want to look up for.
	 * @return  the data for a specific field
	 * 			if the column is not found, return null.
	 */
	public Object getDataForColumn(CustomColumn c){
		try{
			int cIndex = this.schema.getColumnIndex(c);
			return this.data[cIndex];	
		}catch(IllegalArgumentException e){
			// Logger.warnln(e.getMessage());
			return null;
		}
	}

	/**
	 * Return the data type for field
	 * @param c
	 * @return
	 */
	public DATA_TYPE getTypeForColumn(CustomColumn c){
		return this.schema.getTypeForColumn(c);		
	}

	/**
	 * Generate a CSV line representations
	 * @param delimiator
	 * @return
	 */
	public String toCSVString(String delimiator){
		String ret = "";
		for (int i = 0; i < this.data.length; i++){
			ret += data[i].toString();
			if (i != this.data.length - 1){
				ret += delimiator; 
			}else{
				ret += "\n";
			}
		}

		return ret;
	}

	/**
	 * @return how many bytes does this tuple take
	 */
	public int sizeInBytes(){
		return this.data.length*4;
	}

	/**
	 * @return number of attributes
	 */
	public int numOfAttributes(){
		return this.data.length;
	}

	@Override
	public String toString(){
		return toCSVString(",");
	}


	/**
	 * Compares two tuples according to the specified column order (like compareTo with extra argument)
	 * PRECONDITION: fullOrderBy contains EVERY column in the schema, not just the ones in the
	 * original SQL ORDER BY clause
	 * @param fullOrderBy orderBy, including every column in the tuples' schema
	 * @param other the tuple to compare to
	 * @return -1 for less, 0 for equal, 1 for greater
	 */
	public int customCompare(CustomColumn[] fullOrderBy, Tuple other){
		for( CustomColumn c : fullOrderBy){			
			int ret = this.columnwiseCompare(c, c, other);
			if (ret != 0){
				return ret;
			}
			//These columns are equal, loop again.
		}
		return 0;
	}
	
	/**
	 * Compare self.[selfColumn] with other.[otherColumn]
	 * @param selfColumn 	[precondition] selfColumn in 	self.schema
	 * @param otherColumn	[precondition] otherColumn in 	[other].schema
	 * @param other
	 * @return 1,0, -1 if self.[selfColumn] is less than, equal to, or greater than other.[otherColumn]
	 */
	public int columnwiseCompare(CustomColumn selfColumn, CustomColumn otherColumn, Tuple other){
		Integer d1 = (Integer) getDataForColumn( selfColumn );
		Integer d2 = (Integer) other.getDataForColumn( otherColumn );
		if (d1== null||d2==null) {
			
			Logger.warnln("NULL COLUMN DATA: C1:" + d1+",C2:"+d2+":C1"+selfColumn+","+otherColumn+";C2:"+other.getSchema());
		}
		if( (int)d1 > (int)d2){
			return 1;
		} else if ((int)d2 > (int)d1 ){
			return -1;
		}
		return 0;
	}

	//Two tuples are equal if they have the same data in the same format
	@Override
	public boolean equals(Object o){
		if( ! (o instanceof Tuple) ) return false;
		Tuple other = (Tuple) o;
		CustomColumn[] c1 = getSchema().getColumns();
		CustomColumn[] c2 = other.getSchema().getColumns();

		//This case should not be hit as we only utilize equals for distinct selection
		if( ! Arrays.equals(c1,c2) ){
			Logger.warnln("Unexpected columns not equal error in Tuple");
			return false;
		}

		for( CustomColumn c : c1){
			int d1 = (Integer) getDataForColumn( c );
			int d2 = (Integer) other.getDataForColumn( c );
			if( d1 != d2) return false;
		}
		return true;
	}

}
