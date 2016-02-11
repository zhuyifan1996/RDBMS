package operators.logical;


/**
 * This interface specifies the pattern for the logical oeprator visitor
 * @author Guandao Yang
 */
public interface LogicalOperatorVisitor {
	
	/**
	 * Visit a scan operator
	 * @param scan
	 */
	public void visit(ScanOperator scan);
	
	/**
	 * Visit a select operator
	 * @param select
	 */
	public void visit(SelectOperator select);
	
	/**
	 * Visit a projection operator
	 * @param projection
	 */
	public void visit(ProjectionOperator projection);
	
	/**
	 * Visit a join operator
	 * @param join
	 */
	public void visit(JoinOperator join);
	
	/**
	 * Visit a sort operator
	 * @param sortOperator
	 */
	public void visit(SortOperator sort);

	/**
	 * Visit a DuplicateEliminateOperator
	 * @param duplicateEliminateOperator
	 */
	public void visit(DuplicateEliminateOperator dupElimOp);
	
}
