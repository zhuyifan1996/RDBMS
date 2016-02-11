package planner;

import operators.Operator;

/**
 * Planner class defines the abstract SQL Planners that
 * read a fetch query and return a query plan which is represents 
 * by an operator tree.
 * @author Guandao Yang
 * 
 * TODO Hazard in variable hierarchy!
 *
 */
public class Planner{

	protected Operator op;
	
	public Planner(){
		reset();
	}
	
	/**
	 * @return Operator for the parsed tree being visited
	 */
	public Operator getOperator(){
		return this.op;
	}
	
	/**
	 * Reset methods will clean up the data in the planner, 
	 * so that the planner can be reused
	 */
	public void reset(){
		this.op = null;
	}
	
}
