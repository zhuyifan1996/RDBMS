package operators.logical;

import operators.Operator;

/**
 * This interface specifies the visitor model for logical operator
 * @author Guandao Yang
 *
 */
public interface LogicalOperator extends Operator {
	
	/**
	 * Accept the visitor to go through the logical operator tree
	 * @param visitor
	 */
	public void accept(LogicalOperatorVisitor visitor);	
}
