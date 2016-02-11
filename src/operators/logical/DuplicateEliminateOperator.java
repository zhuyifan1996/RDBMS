package operators.logical;

public class DuplicateEliminateOperator extends UnaryLogicalOperator {

	public DuplicateEliminateOperator(LogicalOperator child) {
		super(child);
	}

	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}

	@Override 
	public String toString(){
		return "LogicalDuplicateEliminate("+this.child+")";
	}
	
}
