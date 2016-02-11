package planner;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import db.SystemCatalogue;
import operators.logical.JoinOperator;
import operators.logical.LogicalOperator;
import operators.logical.ScanOperator;
import operators.logical.SelectOperator;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
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
 * SqlWhereExpressionVisitor will visit the WHERE Clause of the SQL
 * Query and construct an operator tree according to the Query.
 * 
 * !Important! 	This visitor should be able to isolate the expressions for
 * 				Join Statement and the Expressions for non-join statement
 * 				and plan accordingly.
 * 
 * Each visitor will produce a tree of operators after its visit,
 * if use this visitor to visit more than once, the tree will chain up
 * (so don't do that!)
 * 
 * @author Guandao Yang
 *
 */
public class SqlWhereExpressionVisitor extends Planner implements ExpressionVisitor {

	/**
	 * Casting the super field op
	 */
	public LogicalOperator op;
	
	/**
	 * The collections of names of all the base tables.
	 */
	protected HashSet<String> baseTableNames;
	
	/**
	 * Mapping form table name of a base table to corresponding ScanOperator
	 */
	protected HashMap<String, ScanOperator> scanners;
	
	/**
	 * An Array of expression that corresponds to all the expressions
	 * that concern only about a single table with {tableNAmesInOrder[i]}
	 */
	protected Expression[] oneTableExpressionsInOrder;
	
	/**
	 * And Array of expression that corresponding to all the expressions 
	 * that concerns the first i tables in tableNamesInOrder array
	 */
	protected Expression[] joinTableExpressionInOrder;
	
	/**
	 * Mapping from table name/alias to the bucket index
	 */
	protected HashMap<String, Integer> nameToBucketIndex;
	
	/**
	 * The mapping from the bucket index to the scan operator
	 * the inverse of name-to-bucketIndex
	 */
	protected HashMap<Integer, ScanOperator> indexToScanner;
	
	/**
	 * EvalNode to put inside the the evaluation stack for constructing
	 * an operator tree with join and select operators
	 * @author Guandao Yang
	 */
	protected class EvalNode{
		/**
		 * The Expression associated with the operator.
		 */
		public Expression expr;
		
		/**
		 * A set of names of the base tables that are relevant 
		 * to the current EvalNode; 
		 */
		public HashSet<String> tableSet;
		
		public EvalNode(){
			this.tableSet = new HashSet<String>();
		}
		
		public String toString(){
			return "("+expr+", "+tableSet+")";
		}
	}

	/**
	 * The stack that helps construct the operator tree
	 */
	protected Stack<EvalNode> evalStack;
	
	/**Default constructor. */
	public SqlWhereExpressionVisitor(){}
	
	/**
	 * 
	 * @param scanners
	 * @param nameToBucketIndex
	 * @param defaultTableName
	 */
	public SqlWhereExpressionVisitor(HashMap<String, ScanOperator> scanners, 
			HashMap<String, Integer> nameToBucketIndex, HashSet<String> baseTableNames){
		this.scanners = scanners;
		this.evalStack = new Stack<EvalNode>();
		this.nameToBucketIndex = nameToBucketIndex;
		this.baseTableNames = baseTableNames;
		
		int numOfIndexes = (new HashSet<Integer>(nameToBucketIndex.values())).size();
		this.oneTableExpressionsInOrder = new Expression[numOfIndexes];
		this.joinTableExpressionInOrder = new Expression[numOfIndexes];
		
		this.indexToScanner = new HashMap<Integer, ScanOperator>();
		for (String tblName: scanners.keySet()){
			Integer bucketIndex = this.nameToBucketIndex.get(tblName);
			ScanOperator scan   = this.scanners.get(tblName);
			this.indexToScanner.put(bucketIndex, scan);
		}
	}

	@Override
	public LogicalOperator getOperator(){
		// build up the whole operators
		if (!this.evalStack.empty()){
			EvalNode node = this.evalStack.pop();
			classifyConjunctChild(node); 
		}
		
		// first, build single operators
		LogicalOperator[] ops = new LogicalOperator[this.oneTableExpressionsInOrder.length];
		for (int i = 0; i < this.oneTableExpressionsInOrder.length; i++){
			if (this.oneTableExpressionsInOrder[i]!= null){
				ScanOperator scan = this.indexToScanner.get(i);
				// ops[i] = new SelectionOperator(scan, this.oneTableExpressionsInOrder[i], this.baseTableNames);
				ops[i] = new SelectOperator(scan, this.oneTableExpressionsInOrder[i], this.baseTableNames);
			}else{
				ops[i] = this.indexToScanner.get(i);
			}
		}
		
		// then build up the join operators
		this.op = ops[0];
		for (int i = 1; i < this.joinTableExpressionInOrder.length; i++){
//			
//			System.out.println("\n"+this.oneTableExpressionsInOrder[i]+"\n");
//			System.out.println(this.joinTableExpressionInOrder[i]+"\n");
			
			JoinOperator join = new JoinOperator(this.op, ops[i]);
			if (this.joinTableExpressionInOrder[i] != null){
				join.setExpression(this.joinTableExpressionInOrder[i]);
				JoinExprDecomposer decomp = new JoinExprDecomposer(this.joinTableExpressionInOrder[i],
						this,this.oneTableExpressionsInOrder.length);
//				System.out.println("[After Decomp]leftCol = "+decomp.leftCol);
//				System.out.println("[After Decomp]rightCol = "+decomp.rightCol);
				
				//TODO : Handle no join condition case: CROSS-PRODUCT
				join.setLeftJoinColumn(decomp.leftCol);
				join.setRightJoinColumn(decomp.rightCol);
			}
			
			this.op = join;
		}
				
		return this.op;
	}

	@Override
	public void visit(Column arg0) {
		String tableName = arg0.getTable().getName();
		// String tableAlias = arg0.getTable().getAlias();
		
		EvalNode newNode = new EvalNode();
		newNode.expr = arg0;
		if (tableName == null){
			//look up table for this column in system catalogue
			try {
				SystemCatalogue sc = SystemCatalogue.getSharedInstance();
				Table tbl = sc.tableForColumn(arg0.getColumnName(), this.baseTableNames);
				newNode.tableSet.add(tbl.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			if (this.scanners.get(tableName) != null){
				newNode.tableSet.add(tableName);
			}else{
				System.err.println("Invalid column:"+arg0);
			}
		}
		
		this.evalStack.push(newNode);
	}
	
	@Override
	public void visit(Parenthesis arg0) {
		arg0.getExpression().accept(this);
	}
	
	/**
	 * None-Boolean-Producer refers to the nodes that satisfies 
	 * 		1. The node itself won't evaluate to a boolean value
	 * 		2. The children of the node won't evaluate to a boolean value 
	 * 
	 * @param expr
	 * @return 	An expression node that collects all the tables that is 
	 * 			involved in this expression, the expression itself, and 
	 * 			null to the operator. 
	 */
	private EvalNode evalNoneBooleanProducer(BinaryExpression expr){
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
		
		EvalNode nodeRight = this.evalStack.pop();
		EvalNode nodeLeft  = this.evalStack.pop();
		
		// Just need to merge the tables of these two columns
		EvalNode newNode = new EvalNode();
		newNode.expr = expr;
		newNode.tableSet.addAll(nodeRight.tableSet);
		newNode.tableSet.addAll(nodeLeft.tableSet);
		
		return newNode;
	}
	
	@Override
	/**
	 * This is a none-boolean-producer 
	 */
	public void visit(Addition arg0) {
		this.evalStack.push(evalNoneBooleanProducer(arg0));
	}

	@Override
	/**
	 * This is a none-boolean-producer 
	 */
	public void visit(Division arg0) {
		this.evalStack.push(evalNoneBooleanProducer(arg0));
	}

	@Override
	/**
	 * THis is an none-boolean-producer
	 */
	public void visit(Multiplication arg0) {
		this.evalStack.push(evalNoneBooleanProducer(arg0));
	}

	@Override
	/**
	 * THis is an none-boolean-producer
	 */
	public void visit(Subtraction arg0) {
		this.evalStack.push(evalNoneBooleanProducer(arg0));
	}

	/**
	 * @param node  [pre] node.tableSet.size() == 1
	 * @return the index for the table of the node
	 */
	private int findSingleTableForNode(EvalNode node){
		// get the first element
		String tableName = node.tableSet.toArray(new String[0])[0];
		return this.nameToBucketIndex.get(tableName);
	}
	
	/**
	 * @param node
	 * @return
	 * 	-2 if the size is 0
	 *  -1 if the size is 1
	 *  index of the conjunct table other wise
	 */
	private void classifyConjunctChild(EvalNode node){
		if (node.tableSet.size() == 0){
			// TODO!
			return;
		}else if (node.tableSet.size() == 1){
			int index = findSingleTableForNode(node);
			if (this.oneTableExpressionsInOrder[index]== null){
				this.oneTableExpressionsInOrder[index] = node.expr;
			}else{
				this.oneTableExpressionsInOrder[index] = new AndExpression(this.oneTableExpressionsInOrder[index], node.expr);	
			}
		}else{
			int pointer = 0;
			for(String tableName: node.tableSet){
				// find the index for the table name
				Integer newIndex = this.nameToBucketIndex.get(tableName);
				pointer = Math.max(newIndex, pointer);
			}
			
			// multi table
			if (this.joinTableExpressionInOrder[pointer]== null){
				this.joinTableExpressionInOrder[pointer] = node.expr;
			}else{
				this.joinTableExpressionInOrder[pointer] = new AndExpression(this.joinTableExpressionInOrder[pointer], node.expr);	
			}
		}
	}
	
	@Override
	/**
	 * Put all the expression in corresponding join/single table 
	 * @param arg0
	 */
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
		
		for (int i = 0; i < 2; i++){
			EvalNode node = this.evalStack.pop();
			classifyConjunctChild(node); 
		}
		
		// need to push something inside
		EvalNode newNode = new EvalNode();
		EqualsTo eq = new EqualsTo();
		LongValue l = new LongValue("1");
		eq.setLeftExpression(l);
		eq.setRightExpression(l);
		newNode.expr = eq;
		this.evalStack.push(newNode);
	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO better way to do it!
		evalBinaryExpression(arg0);
	}
	
	/**
	 * 
	 * @param expr
	 */	
	private void evalBinaryExpression(BinaryExpression expr){
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
		
		EvalNode rightNode = this.evalStack.pop();
		EvalNode leftNode  = this.evalStack.pop();
		
		HashSet<String> totalTables = leftNode.tableSet;
		totalTables.addAll(rightNode.tableSet);
		
		EvalNode newNode = new EvalNode();
		newNode.expr = expr;
		newNode.tableSet = totalTables;
		
		this.evalStack.push(newNode);
	}

	@Override
	/**
	 * Use post-order traversal.
	 * Should pop two nodes and look into in whether I should use 
	 * a join operator or a selection operator to evaluate this equalsTo
	 * @param arg0
	 */
	public void visit(EqualsTo arg0) {
		evalBinaryExpression(arg0);
	}

	@Override
	public void visit(GreaterThan arg0) {
		evalBinaryExpression(arg0);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		evalBinaryExpression(arg0);
	}

	@Override
	public void visit(MinorThan arg0) {
		evalBinaryExpression(arg0);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		evalBinaryExpression(arg0);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		evalBinaryExpression(arg0);
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
	public void visit(Between arg0) {
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
	
	/**
	 * Evaluate the terminal expressions by simply
	 * create a node.
	 * @param expr
	 */
	private void evalTerminalExpression(Expression expr){
		EvalNode newNode = new EvalNode();
		newNode.expr = expr;
		this.evalStack.push(newNode);
	}
	
	@Override
	public void visit(NullValue arg0) {
		evalTerminalExpression(arg0);
	}
	
	@Override
	public void visit(DoubleValue arg0) {
		evalTerminalExpression(arg0);
	}

	@Override
	public void visit(LongValue arg0) {
		evalTerminalExpression(arg0);
	}

	@Override
	public void visit(DateValue arg0) {
		evalTerminalExpression(arg0);
	}

	@Override
	public void visit(TimeValue arg0) {
		evalTerminalExpression(arg0);
	}

	@Override
	public void visit(TimestampValue arg0) {
		evalTerminalExpression(arg0);
	}

	@Override
	public void visit(StringValue arg0) {
		evalTerminalExpression(arg0);
	}
	
}
