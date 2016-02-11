package operators.logical;

import operators.AbstractOperator;
import operators.Operator;
import operators.UnaryOperator;

public abstract class UnaryLogicalOperator extends AbstractOperator implements UnaryOperator, LogicalOperator {
	
	public LogicalOperator child;
	
	public UnaryLogicalOperator(LogicalOperator child) {
		super(child.getSchema());
		this.child = child;
	}
	
	@Override
	public LogicalOperator getChild(){
		return this.child;
	}

	@Override
	public void setChild(Operator child) {
		this.child = (LogicalOperator)child;
	}

}
