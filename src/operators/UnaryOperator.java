package operators;

/**
 * This is an abstraction for Unary Operator of the Relational Algebra
 * @author Guandao Yang
 */
public interface UnaryOperator{

	/**
	 * @return The child operator
	 */
	public Operator getChild();
	
	/**
	 * Update the child for the operator. 
	 * This function will reset the operator automatically
	 * @param child
	 */
	public void setChild(Operator child);

}
