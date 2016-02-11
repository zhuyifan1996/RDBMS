package operators.logical;

import java.util.HashSet;

import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends UnaryLogicalOperator {

	/**
	 * The expression on which the tuple is filtered
	 */
	public final Expression expr;
	public final HashSet<String> baseTableNames;

	public SelectOperator(LogicalOperator child, Expression expr, HashSet<String> baseTableNames) {
		super(child);
		this.expr = expr;
		this.baseTableNames = baseTableNames;
	}

	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		return "LogicalSelection("+this.child+","+this.expr+")";
	}

}
