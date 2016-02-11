package planner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import db.CustomColumn;
import db.SystemCatalogue;
import operators.logical.LogicalOperator;
import operators.logical.SortOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

/**
 * 
 * @author Trevor Edwards
 *
 */
public class SqlOrderByVisitor implements OrderByVisitor {

	private LogicalOperator op;
	private ArrayList<CustomColumn> cc;
	private HashSet<String> baseTableNames;

	public SqlOrderByVisitor(LogicalOperator op, HashSet<String> baseTableNames) {
		this.op = op;
		cc = new ArrayList<CustomColumn>();
		this.baseTableNames = baseTableNames;
	}

	@Override
	public void visit(OrderByElement obe) {

		Column e = (Column) obe.getExpression();
		CustomColumn newCol = null;
		
		//If no table reference exists in column e, look up the table
		if (e.getTable().getName()==null){
			try {
				SystemCatalogue sc = SystemCatalogue.getSharedInstance();
				Table tbl = sc.tableForColumn(e.getColumnName(), this.baseTableNames);
				e.setTable( tbl );;
				newCol = new CustomColumn(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else{
			newCol = new CustomColumn(e);
		}
		
		cc.add(newCol);
	}

	public SortOperator getOperator(){
		return new SortOperator(op, cc.toArray(new CustomColumn[1]));
	}
}
