package planner;

import operators.logical.ScanOperator;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * From Item Visitor will visit the from item and make a planning
 * by construct the Scan operator. If the visitor was used
 * to visit more than one from item, the operator will be chained up.
 * (So hopefully people don't do thats)
 * @author Guandao Yang
 *
 */
public class SqlFromItemVisitor implements FromItemVisitor{
	
	private ScanOperator op;
	private Table tbl;
	
	public SqlFromItemVisitor() {
		super();
	}
	
	/**
	 * @return The table from the FromItem.
	 */
	public Table getTable(){
		return this.tbl;
	}

	@Override
	public void visit(Table arg0) {
		this.tbl = arg0;
		this.op = new ScanOperator(arg0);
	}
	
	public ScanOperator getOperator(){
		return this.op;
	}

	@Override
	public void visit(SubSelect arg0) {
		throw new Error("Unimpelmented method");
	}

	@Override
	public void visit(SubJoin arg0) {
		throw new Error("Unimpelmented method");
	}

}
