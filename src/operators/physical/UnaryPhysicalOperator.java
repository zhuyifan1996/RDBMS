package operators.physical;

import operators.AbstractOperator;
import operators.Operator;
import operators.UnaryOperator;

public abstract class UnaryPhysicalOperator extends AbstractOperator implements 
	UnaryOperator, PhysicalOperator {

	public PhysicalOperator child;
	
	public UnaryPhysicalOperator(PhysicalOperator child) {
		super(child.getSchema());
		this.child = child;
	}
	
	@Override
	public PhysicalOperator getChild(){
		return this.child;
	}
	
	@Override
	public void setChild(Operator child){
		this.child = (PhysicalOperator)child;
	}
	
}
