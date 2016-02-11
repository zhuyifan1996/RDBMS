package operators.physical;

import java.io.IOException;
import java.util.HashSet;
import java.util.Stack;

import db.CustomColumn;
import db.DATA_TYPE;
import db.SystemCatalogue;
import db.Tuple;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * This class helps to evaluate the expression clause 
 * and determine the outcome of the expression
 * @author Guandao Yang
 * TODO this also need to know the schema information, 
 * shall we put that information inside the tuple?
 */
public class SingleExpressionEvaluator implements ExpressionVisitor, Evaluator {

	/**
	 * The tuple on which the expression is evaluated
	 */
	protected Tuple tp;	
	private HashSet<String> baseTableNames;

	/**
	 * The evaluation stack where you can put everything
	 */
	protected Stack<EvalResult> evalStack;
	
	protected class EvalResult {
		public Object val;
		public DATA_TYPE type;
		
		public EvalResult(Object val, DATA_TYPE type){
			this.val = val;
			this.type = type;
		}
		
		public String toString(){
			return "EvalRet{"+this.val+", "+this.type+"}";
		}
	}
		
	/**
	 * Construct the Expression Evaluator using only one tuple
	 * (Not for joint operation)
	 * @param tp
	 */
	public SingleExpressionEvaluator(Tuple tp, HashSet<String> baseTableNames) {
		this.tp = tp;
		this.evalStack = new Stack<EvalResult>();
		this.baseTableNames = baseTableNames;
	}
	
	public SingleExpressionEvaluator() {
		this.evalStack = new Stack<EvalResult>();
	}
	
	/**
	 * @return the result of the evaluation.
	 */
	public boolean getResult(){
		EvalResult ret = this.evalStack.pop();
		if (ret.type == DATA_TYPE.BOOL){
			return ((Boolean)ret.val).booleanValue();
		}
		
		return false;
	}

	@Override
	public void visit(NullValue arg0) {
		evalStack.push(new EvalResult(arg0, DATA_TYPE.NULL));
	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DoubleValue arg0) {
		evalStack.push(new EvalResult(arg0.getValue(), DATA_TYPE.DOUBLE));
	}

	@Override
	public void visit(LongValue arg0) {
		Integer l = new Integer(((int)arg0.getValue()));
		evalStack.push(new EvalResult(l, DATA_TYPE.LONG));
	}

	@Override
	public void visit(DateValue arg0) {
		evalStack.push(new EvalResult(arg0.getValue(), DATA_TYPE.DATE));
	}

	@Override
	public void visit(TimeValue arg0) {
		evalStack.push(new EvalResult(arg0.getValue(), DATA_TYPE.TIME));
	}

	@Override
	public void visit(TimestampValue arg0) {
		evalStack.push(new EvalResult(arg0.getValue(), DATA_TYPE.TIMESTAMP));
	}

	@Override
	public void visit(Parenthesis arg0) {
		arg0.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue arg0) {
		evalStack.push(new EvalResult(arg0.getValue(), DATA_TYPE.STRING));
	}

	@Override
	public void visit(Addition arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				// Then only DOuble and Integer can be added together
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(new Double(d1+d2), DATA_TYPE.DOUBLE));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(new Integer(l1+l2), DATA_TYPE.LONG));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}	
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(Division arg0) {
		// Post Order Traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: SHOULD BE e2/e1
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (!checkNull(e1,e2)){
				// Then only DOuble and Long can be added together
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2/d1, DATA_TYPE.DOUBLE));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(new Integer(l2/l1), DATA_TYPE.LONG));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}
			}
		}catch(IllegalArgumentException e){
			System.err.println("Operate one two different types.");
		}
	}

	@Override
	public void visit(Multiplication arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (!checkNull(e1,e2)){
				// Then only DOuble and Long can be added together
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d1*d2, DATA_TYPE.DOUBLE));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(new Integer(l1*l2), DATA_TYPE.LONG));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
				}
			}
		}catch(IllegalArgumentException e){
			System.err.println("Operate one two different types.");
		}
	}
	
	@Override
	public void visit(Subtraction arg0) {
		// Post Order Traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: SHOULD BE e2/e1
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (!checkNull(e1,e2)){
				// Then only DOuble and Long can be added together
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2-d1, DATA_TYPE.DOUBLE));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(new Integer(l2-l1), DATA_TYPE.LONG));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}
			}
		}catch(IllegalArgumentException e){
			System.err.println("Operate one two different types.");
		}
		
	}

	@Override
	public void visit(AndExpression arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (!checkNull(e1,e2)){
				// Only BOOL can be evaluated
				switch(e1.type){
					case BOOL:
						boolean b1 = ((Boolean)e1.val).booleanValue();
						boolean b2 = ((Boolean)e2.val).booleanValue();
						this.evalStack.push(new EvalResult(b1 && b2, DATA_TYPE.BOOL));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
				}
			}
		}catch(IllegalArgumentException e){
			System.err.println("Operate one two different types.");
		}
	}

	@Override
	public void visit(OrExpression arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		// type check
		if (e1.type != e2.type && e1.type != DATA_TYPE.NULL && e2.type != DATA_TYPE.NULL){
			System.err.println("Invalid type");
		}
		
		// Starts Evaluation
		// First: check NULL
		if (e1.type == DATA_TYPE.NULL){
			evalStack.push(e1);
		}else if (e2.type == DATA_TYPE.NULL){
			evalStack.push(e2);
		}else{
			// Only BOOL can be evaluated
			switch(e1.type){
				case BOOL:
					boolean b1 = ((Boolean)e1.val).booleanValue();
					boolean b2 = ((Boolean)e2.val).booleanValue();
					this.evalStack.push(new EvalResult(b1 || b2, DATA_TYPE.BOOL));
					break;
				default:
					// TODO 
					System.err.println("Invalid type: Expected Bool, but get "+e1.type);
					break;
				
			}
		}
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				boolean ret = e1.val.equals(e2.val);
				evalStack.push(new EvalResult(ret, DATA_TYPE.BOOL));
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(GreaterThan arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: should be (e2 > e1)
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				// TODO should also implement for TIME/DATE/TIMESTAMP
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2 > d1, DATA_TYPE.BOOL));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(l2 > l1, DATA_TYPE.BOOL));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}	
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: should be (e2 >= e1)
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				// TODO should also implement for TIME/DATE/TIMESTAMP
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2 >= d1, DATA_TYPE.BOOL));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(l2 >= l1, DATA_TYPE.BOOL));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}	
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinorThan arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: should be (e2 < e1)
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				// TODO should also implement for TIME/DATE/TIMESTAMP
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2 < d1, DATA_TYPE.BOOL));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(l2 < l1, DATA_TYPE.BOOL));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}	
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		// ATTENTION: should be (e2 <= e1)
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				// TODO should also implement for TIME/DATE/TIMESTAMP
				switch(e1.type){
					case DOUBLE:
						double d1 = Double.valueOf((Integer)e1.val);
						double d2 = Double.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(d2 <= d1, DATA_TYPE.BOOL));
						break;
					case LONG:
						int l1 = Integer.valueOf((Integer)e1.val);
						int l2 = Integer.valueOf((Integer)e2.val);
						evalStack.push(new EvalResult(l2 <= l1, DATA_TYPE.BOOL));
						break;
					default:
						// TODO 
						System.err.println("Invalid type");
						break;
					
				}	
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// First, do the post-order traversal
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		EvalResult e1 = this.evalStack.pop();
		EvalResult e2 = this.evalStack.pop();
		
		try{
			if (! checkNull(e1,e2)){
				boolean ret = !e1.val.equals(e2.val);
				evalStack.push(new EvalResult(ret, DATA_TYPE.BOOL));
			}
		}catch (IllegalArgumentException e){
			System.err.println("Two types doesn't match");
			e.printStackTrace();
		}
	}

	@Override
	public void visit(Column arg0) {
		String tableName = arg0.getTable().getName();

		if (tableName == null){
//			newNode.tableSet.add(this.defaultTableName);
			//look up table for this column in system catalogue
			try {
				SystemCatalogue sc = SystemCatalogue.getSharedInstance();
				Table tbl = sc.tableForColumn(arg0.getColumnName(), this.baseTableNames);
				arg0.setTable(tbl);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		CustomColumn c = new CustomColumn(arg0);
		Object obj = this.tp.getDataForColumn(c);
		DATA_TYPE type = this.tp.getTypeForColumn(c);

		this.evalStack.push(new EvalResult(obj, type));
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * (Helper) This function checks whether the expression is a null related 
	 * logics if one of them is a null, then push a null in stack and return 
	 * true; otherwise, return false.
	 * 
	 * @param e1	Left  EvalResult	(the closer one)
	 * @param e2 	Right EvalResult 	(the deeper one)
	 * @return whether it is a null related logics
	 * @throws IllegalArgumentException
	 */
	private boolean checkNull(EvalResult e1, EvalResult e2) throws IllegalArgumentException{
		// type check
		if (e1.type != e2.type && e1.type != DATA_TYPE.NULL && e2.type != DATA_TYPE.NULL){
			throw new IllegalArgumentException("Types on binary operations isn't consistent:" + e1 +","+e2);
		}
		
		// First: check NULL
		if (e1.type == DATA_TYPE.NULL){
			evalStack.push(e1);
			return true;
		}else if (e2.type == DATA_TYPE.NULL){
			evalStack.push(e2);
			return true;
		}
		return false;
		
	}
}
