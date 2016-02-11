package operators.logical;

import java.io.IOException;


import operators.AbstractOperator;
import net.sf.jsqlparser.schema.Table;
import db.CustomColumn;
import db.SchemaWithDictionary;
import db.SystemCatalogue;
import main.Logger;
public class ScanOperator extends AbstractOperator implements LogicalOperator {

	private SystemCatalogue scat;	
	private Table tbl;
	
	public ScanOperator(Table tbl) {
		super(null);
		this.tbl = tbl;
		try {
			scat = SystemCatalogue.getSharedInstance();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// set the schema for the current scan
		this.schema = scat.getSchemaForTable(tbl);
		if( this.schema == null)
			Logger.warnln("Scan Operator could not find a schema for "+tbl);
	}
	
	public void setAlias(String alias){
		//TODO: verify this doesn't break anything
		if( schema != null){
			CustomColumn[] cols = this.schema.getColumns();
			this.schema = new SchemaWithDictionary(cols, alias, this.tbl.getName());
		}else{
			Logger.warnln("Missing a schema when setting alias "+alias);
		}
	}

	/**
	 * @return The table of this scan operator
	 */
	public Table getTable(){
		return this.tbl;
	}
	
	public CustomColumn[] getColumns(){
		return this.schema.getColumns();
	}

	public void setTable(Table tbl){
		this.tbl = tbl;
	}
	
	@Override
	public ScanOperator clone(){
		return new ScanOperator(this.tbl);
	}
	
	@Override
	public String toString(){
		return "LogicalScan("+this.tbl.getName()+", " + this.tbl.getAlias()+", "+this.getSchema()+ ", "+ super.toString().substring(30) +")";
	}
	
	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}

}
