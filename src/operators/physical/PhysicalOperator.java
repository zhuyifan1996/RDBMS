package operators.physical;

import operators.Operator;
import db.Tuple;

/**
 * This interface represents the physical operator 
 * To be exact, specifies the iterator model a physical
 * operator should have
 * @author Guandao Yang
 *
 */
public interface PhysicalOperator extends Operator {

	/**
	 * Resets the operator, so that the next call of getNextTuple will
	 * return the very first tuple computed by the operators.
	 */
	public abstract void reset();

	/**
	 * Return the next tuple computed by this operator
	 * Return null if there are no more tuples to be returned.
	 */
	public abstract Tuple getNextTuple(); 

}
