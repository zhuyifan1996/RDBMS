package operators.physical;

import java.util.HashSet;
import net.sf.jsqlparser.expression.Expression;
import db.Tuple;

public class SelectionOperator extends UnaryPhysicalOperator{

	/**
	 * The expression on which the tuple is filtered
	 */
	private Expression expr;
	private HashSet<String> baseTableNames;
	
	public SelectionOperator(PhysicalOperator child, Expression expr, HashSet<String> baseTableNames){
		super(child);
		this.expr = expr;
		this.baseTableNames = baseTableNames;
	}
	
	@Override
	public void reset() {
		child.reset();
	}

	@Override
	public Tuple getNextTuple() {
		Tuple t = null;
		while((t = this.child.getNextTuple()) != null){
			SingleExpressionEvaluator eval = new SingleExpressionEvaluator(t, this.baseTableNames);
			this.expr.accept(eval);
			if (eval.getResult()){
				return t;
			}			
		}
		return null;
	}

	@Override
	public String toString(){
		return "Selection("+this.child+","+this.expr+")";
	}
}
