package operators;

import db.Schema;

/**
 * This abstract class specifies the method an operator should have
 * @author Guandao Yang
 */
public interface Operator {
	
	/**
	 * @return return the schema corresponding to the oeprator
	 */
	public Schema getSchema();
	
}
