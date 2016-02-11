package planner;


import java.util.ArrayList;
import java.util.HashMap;

import db.CustomColumn;
import operators.logical.LogicalOperator;
import operators.logical.ProjectionOperator;
import operators.logical.ScanOperator;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

/**
 * Seach of the SlectItem visitor can visit a select item and create a single
 * projection operator corresponding to the selection item. If you used the same
 * visitor to visit multiple selection item, then the operator will be chained up
 * into a series of selection.
 * @author Guandao Yang
 *
 */
public class SqlSelectItemVisitor extends Planner implements SelectItemVisitor {
	
	/**
	 * The default table of the SqlSelection. If no table was specified,
	 * will use the default table scanner as the basic ScannerOperation
	 * 
	 */
	private Table defaultTable;
	private LogicalOperator defaultOp;
	private HashMap<String, ScanOperator> scanners;
	private ArrayList<CustomColumn> cols;
	private boolean isAll = false;
	
	public SqlSelectItemVisitor(LogicalOperator defaultOp, Table tbl, 
			HashMap<String, ScanOperator>scanners) {
		super();
		this.defaultOp = defaultOp;
		this.cols = new ArrayList<CustomColumn>();
		setDefaultScanner(tbl, scanners);
	}
	
	/**
	 * This function sets the default scan operator
	 * this function will not reset the visitor, but after
	 * we set the new default scanner, the visitor will produce
	 * selection operations with this default scanner.
	 * @param scanner
	 */
	public void setDefaultScanner(Table tbl, HashMap<String, ScanOperator>scanners){
		this.defaultTable = tbl;
		this.scanners = scanners;
	}
 
	@Override
	public ProjectionOperator getOperator(){
		if (this.isAll){
			return null;
		}else{
			CustomColumn[] col = this.cols.toArray(new CustomColumn[1]);
			return new ProjectionOperator(defaultOp, col);
		}
	}
	
	@Override
	/**
	 * If we just want all columns, then we will return any selection operator
	 * @param arg0
	 */
	public void visit(AllColumns arg0) {
		this.op = null;
		this.cols = null;
		this.isAll = true;
	}

	@Override
	public void visit(AllTableColumns arg0) {
		if (! this.isAll){
			ScanOperator s = null; 
			if (this.scanners.get(arg0.getTable().getName()) != null){
				s = this.scanners.get(arg0.getTable().getName());
				for (CustomColumn c: s.getColumns()){
					this.cols.add(c);
				}
			}else{
				System.err.println("Inalid table name:"+arg0.getTable().getName());
			}
		}
	}

	@Override
	public void visit(SelectExpressionItem arg0) {
		if (! this.isAll){
			SqlSelectItemExpressionVisitor v = new SqlSelectItemExpressionVisitor(this.defaultTable);
			arg0.getExpression().accept(v);
			this.cols.add(v.getColumn());
			
			// TODO deal with Alias
		}
	}

}
