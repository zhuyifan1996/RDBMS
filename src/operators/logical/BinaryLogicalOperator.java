package operators.logical;

import operators.AbstractOperator;
import operators.BinaryOperator;
import operators.Operator;

public abstract class BinaryLogicalOperator extends AbstractOperator implements LogicalOperator, BinaryOperator {

	public LogicalOperator leftChild;
	public LogicalOperator rightChild;
	
	public BinaryLogicalOperator(LogicalOperator leftChild, LogicalOperator rightChild) {
		super(null);
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	
	@Override
	public LogicalOperator getLeftChild(){
		return this.leftChild;
	}
	
	@Override
	public LogicalOperator getRightChild(){
		return this.rightChild;
	}

	@Override
	public void setLeftChild(Operator leftChild) {
		this.leftChild = (LogicalOperator)leftChild;
	}

	@Override
	public void setRightChild(Operator rightChild) {
		this.rightChild = (LogicalOperator)rightChild;
	}

}
