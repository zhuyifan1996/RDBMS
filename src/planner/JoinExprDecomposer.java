package planner;

import java.util.ArrayList;

import db.CustomColumn;
import main.Logger;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;

/**
 * Utility class to decompose a binary expression that concerns TWO tables, i.e. a join condition,
 * into two relevant columns.
 * @author Alvin Zhu
 */
public class JoinExprDecomposer extends SqlWhereExpressionVisitor{

	//Join condition
	private Expression expr;
	//The SqlWhereExpressionVisitor that creates this decomposer.
	//Used to retrieve appropriate information
	private SqlWhereExpressionVisitor visitor;

	//[inv]: after evaluating an atomic expression(i.e. expr separated by AND)
	//both leftCol and rightCol should have length col_number
	private int col_number;
	public ArrayList<CustomColumn> leftCol;
	public ArrayList<CustomColumn> rightCol;

	public JoinExprDecomposer(Expression expr, SqlWhereExpressionVisitor visitor,
			int numOfTables ){
		super();
		this.expr = expr;
		this.visitor = visitor;

		this.col_number = 1;
		this.leftCol = new ArrayList<CustomColumn>();
		this.rightCol = new ArrayList<CustomColumn>();

		try{
			//doomed to fail
			this.decompose();
		}catch(Exception e){
			//			System.out.println(this.leftCol);
			//			System.out.println(this.rightCol);
		}

		Logger.println("Finished decomposing");
	}

	/**Visit this.expr, and set leftCol and rightCol to be the appropriate join attribute 
	 * of the appropriate tables.
	 */
	private void decompose(){
		this.expr.accept(this);
	}

	@Override
	public void visit(AndExpression arg0) {
		this.col_number++;
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Column arg0) {
		Logger.println("[Decomp]Visiting column "+arg0);
		Logger.println("[Decomp]this.leftCol = "+this.leftCol);
		Logger.println("[Decomp]this.rightCol = "+this.rightCol);
		if (this.leftCol.size() < this.col_number){
			this.leftCol.add(new CustomColumn(arg0)) ;
		}
		else if(this.rightCol.size() < this.col_number){
			this.rightCol.add(new CustomColumn(arg0)) ;
		}
	}
}
