package operators.physical;

import main.Main;
import net.sf.jsqlparser.schema.Table;

import java.io.IOException;

import operators.AbstractOperator;
import db.Schema;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleReader;

/**
 * This is an abstraction for the Scan operator,
 * this is the leaf operator
 * @author Guandao Yang, Trevor Edwards
 *
 */
public class ScanOperator extends AbstractOperator implements PhysicalOperator {
	
	private SystemCatalogue syscat;
	private TupleReader tpReader;
	public final Table tbl;
	
	public ScanOperator(Table tbl, Schema schema) {
		super(schema);		
		this.tbl = tbl;
		
		try {
			syscat = SystemCatalogue.getSharedInstance();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		this.tpReader = Main.tupleReaderFactory(syscat.getFileForTable(tbl), schema);
	}

	@Override
	public void reset() {
		this.tpReader = Main.tupleReaderFactory(syscat.getFileForTable(tbl), schema);		
	}

	@Override
	public Tuple getNextTuple() {
		return this.tpReader.getNextTuple();
	}
	
	@Override
	public String toString(){
		return "PhysicalScan("+this.tbl.getName()+", " + this.tbl.getAlias()+", "+ super.toString().substring(31) +")";
	}

}
