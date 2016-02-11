package operators;

/**
 * BinaryOperator abstracts the relational operators that 
 * operates on two relations
 * @author Guandao Yang
 * 
 */
public interface BinaryOperator{
	/**
	 * @return The left child operator
	 */
	public Operator getLeftChild();
	
	/**
	 * @return Return the right child operator
	 */
	public Operator getRightChild();
	
	/**
	 * This will set the left child.
	 * This function will also reset the operator
	 * @param leftChild
	 */
	public void setLeftChild(Operator leftChild);
	
	/**
	 * This will set the right child
	 * and also reset the whole operator
	 * @param rightChild
	 */
	public void setRightChild(Operator rightChild);
}
