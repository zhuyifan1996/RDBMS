package db;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * A column identifies a field in an arbitrary table. IMMUTABLE.
 * @author Alvin Zhu
 */
public class CustomColumn{
	
	private String name;
	private DATA_TYPE type;
	private Table tbl;
	
	/**
	 * Constructor for a column
	 * @param name	The field name of the column
	 * @param type	The type of the column. 
	 * @param table: name of the table where this column comes from
	 */
	public CustomColumn(String name, DATA_TYPE type, String table){
		this.name = name;
		this.tbl = new Table();
		this.tbl.setName(table);
		this.type  = type;
	}

	public CustomColumn clone(){
		CustomColumn c = new CustomColumn(this.name, this.type, this.tbl.getName());
		if (this.tbl.getAlias() != null){
			c.tbl.setAlias(this.tbl.getAlias());
		}
		return c;
	}
	
	/**
	 * This is a temporary constructor 
	 * @param c
	 */
	public CustomColumn(Column c){
		this.name = c.getColumnName();
		this.type = DATA_TYPE.LONG;
		this.tbl = c.getTable();
	}
	
	public String getName(){
		return this.name;
	}
	
	public DATA_TYPE getType(){
		return this.type;
	}
	
	public Table getTable(){
		return this.tbl;
	}
	
	
	/*Some examples:
	 * (A,S1,null) = (A, Sailors, S1)
	 * (A,Sailors,S1)!=(A,Sailors,S2)
	 * (A,Sailors, null) = (A,Sailors,null)
	 * (A,Sailors, null) = (A,Sailors,S)  <---- This is the case for "Select Sailors.A from Sailors S"
	 * */
	@Override
	public boolean equals(Object obj){
		if (obj instanceof CustomColumn){
			CustomColumn other = (CustomColumn) obj;
			if (this.tbl.getAlias()==null){
				return other.name.equals(this.name) &&
					   other.getType() == this.getType() &&
					   (this.tbl.getName().equals(other.tbl.getName()) ||
					    this.tbl.getName().equals(other.tbl.getAlias()));
			}else{
				return 	other.name.equals(this.name) &&
						other.getType() == this.getType() &&
						this.tbl.getName().equals(other.tbl.getName()) &&
						this.tbl.getAlias().equals(other.tbl.getAlias());
			}
		}
		return false;
	}
	
	@Override
	/**
	 * This hashCode was calculated by the combination of both the hash codes
	 * of the table name, the column name, and the type index
	 * TODO this might not be a really good hash function
	 */
	public int hashCode(){
		return this.name.hashCode();
	}
	
	@Override
	public String toString(){
		return "(" + this.name + ", " + this.tbl.getName() + ", " + this.tbl.getAlias()+", "+ super.toString().substring(9)+")"; 
	}
	
}
