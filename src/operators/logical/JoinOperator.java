package operators.logical;
import java.util.ArrayList;

import db.CustomColumn;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import operators.util.OperatorsUtil;

public class JoinOperator extends BinaryLogicalOperator {

	/**
	 * The join expression for the operator
	 */
	private Expression expr;
	private CustomColumn[] left_join_col;
	private CustomColumn[] right_join_col;

	public JoinOperator(LogicalOperator leftChild, LogicalOperator rightChild, Expression expr, CustomColumn[] col_left, CustomColumn[] col_right) {
		super(leftChild, rightChild);
		this.expr = expr;
		this.schema = OperatorsUtil.glueSchema(leftChild.getSchema(),rightChild.getSchema());
		this.left_join_col = col_left;
		this.right_join_col = col_right;
	}
	
	public JoinOperator(LogicalOperator leftChild, LogicalOperator rightChild, Expression expr) {
		super(leftChild, rightChild);
		this.expr = expr;
		this.schema = OperatorsUtil.glueSchema(leftChild.getSchema(),rightChild.getSchema());
	}

	public JoinOperator(LogicalOperator leftChild, LogicalOperator rightChild) {
		super(leftChild, rightChild);
		//Set default expression to 1=1
		LongValue l = new LongValue("1"); 
		EqualsTo eq = new EqualsTo();
		eq.setLeftExpression(l);
		eq.setRightExpression(l);
		this.expr = eq;
	}

	public CustomColumn[] getLeftJoinColumn(){
		return this.left_join_col;
	}
	
	public CustomColumn[] getRightJoinColumn(){
		return this.right_join_col;
	}
	
	public void setLeftJoinColumn(CustomColumn[] c){
		this.left_join_col = c;
	}
	
	public void setRightJoinColumn(CustomColumn[] c){
		this.right_join_col = c;
	}
	
	public void setLeftJoinColumn(ArrayList<CustomColumn> c){
		this.left_join_col = c.toArray(new CustomColumn[1]);
	}
	
	public void setRightJoinColumn(ArrayList<CustomColumn> c){
		this.right_join_col = c.toArray(new CustomColumn[1]);
	}
	
	/**
	 * Set the expression of the join operator.
	 * @param expr
	 */
	public void setExpression(Expression expr){
		this.expr = expr;
	}

	/**
	 * @return the join epression
	 */
	public Expression getExpression(){
		return this.expr;
	}

	@Override
	public void accept(LogicalOperatorVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "LogicalJoin(" + this.leftChild.toString()+", " +this.expr+", "+this.rightChild.toString() +")";
	}
}
