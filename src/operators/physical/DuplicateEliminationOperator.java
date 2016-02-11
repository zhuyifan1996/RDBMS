package operators.physical;

import db.Tuple;

/**
 * A DuplicateEliminationOperator removes duplicates, assuming it is placed on top of a SortOperator
 * @author Trevor Edwards
 *
 */
public class DuplicateEliminationOperator extends UnaryPhysicalOperator{
	
	private Tuple last;

	/**
	 * Creates the operator
	 * @param child Must be a SortOperator
	 */
	public DuplicateEliminationOperator(PhysicalOperator child) {
		super(child);
		assert child instanceof SortOperator;
	}

	@Override
	public void reset() {		
		child.reset();
		last = null;
	}

	//Assumes the child returns tuples in sorted order.
	@Override
	public Tuple getNextTuple() {
		while(true){
			Tuple tuppy = child.getNextTuple();
			if( tuppy != null){
				if( !tuppy.equals(last)){
					last = tuppy;
					return tuppy;
				}
			} else 
				return null;
		}
	}
	
	@Override 
	public String toString(){
		return "PhysicalDuplicateEliminate("+this.child+")";
	}
}
