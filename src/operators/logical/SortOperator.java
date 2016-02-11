package operators.logical;

import db.CustomColumn;

public class SortOperator extends UnaryLogicalOperator {

	public final CustomColumn[] orderBy;

	public SortOperator(LogicalOperator child, CustomColumn[] orderBy) {
		super(child);
		this.orderBy = orderBy;		
	}

	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		String stringOrderBy = "null";
		if (this.orderBy != null){
			stringOrderBy = "[";
			for (int i = 0 ; i < this.orderBy.length; i++){
				stringOrderBy+=this.orderBy[i].toString();
				if (i!= this.orderBy.length- 1){
					stringOrderBy+=",";
				}
			}
			stringOrderBy+="]";			
		}

		return "LogicalSort("+stringOrderBy+", "+this.child+")";
	}

}
