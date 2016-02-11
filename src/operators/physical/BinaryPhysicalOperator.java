package operators.physical;

import operators.AbstractOperator;
import operators.BinaryOperator;
import operators.Operator;

public abstract class BinaryPhysicalOperator extends AbstractOperator implements BinaryOperator, PhysicalOperator {
	
	public PhysicalOperator leftChild;
	public PhysicalOperator rightChild;
	
	public BinaryPhysicalOperator(){super(null);}
	
	public BinaryPhysicalOperator(PhysicalOperator leftChild, PhysicalOperator rightChild) {
		super(null);
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}

	@Override
	public Operator getLeftChild() {
		return this.leftChild;
	}

	@Override
	public Operator getRightChild() {
		return this.rightChild;
	}

	@Override
	public void setLeftChild(Operator leftChild) {
		this.leftChild = (PhysicalOperator)leftChild;
	}

	@Override
	public void setRightChild(Operator rightChild) {
		this.rightChild = (PhysicalOperator)rightChild;
	}

}
