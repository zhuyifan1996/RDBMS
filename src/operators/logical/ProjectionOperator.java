package operators.logical;

import db.CustomColumn;

public class ProjectionOperator extends UnaryLogicalOperator {

	public final CustomColumn[] cols;
	
	public ProjectionOperator(LogicalOperator child, CustomColumn[] cols) {
		super(child);
		this.cols = cols;
	}

	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		String cols = "[";
		for (CustomColumn c : this.cols){
			cols += c.getTable().getName()+"."+c.getName()+", ";
		}
		cols+="]";
		
		return "LogicalProjection("+this.child +", "+cols+")";
	}
}
