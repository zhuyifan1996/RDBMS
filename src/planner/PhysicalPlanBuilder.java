package planner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

import main.Logger;
import main.Main;
import db.SystemCatalogue;
import operators.logical.DuplicateEliminateOperator;
import operators.logical.JoinOperator;
import operators.logical.LogicalOperatorVisitor;
import operators.logical.ProjectionOperator;
import operators.logical.ScanOperator;
import operators.logical.SelectOperator;
import operators.logical.SortOperator;
import operators.physical.BNLJoinOperator;
import operators.physical.PhysicalOperator;
import operators.physical.SMJoinOperator;
import operators.physical.SimpleJoinOperator;

/**
 * Traverse the Logical Tree and produce a physical plan
 * @author Guandao Yang
 *
 */
public class PhysicalPlanBuilder implements LogicalOperatorVisitor {

	/**
	 * Class where to put into the stack
	 * @author Guandao Yang
	 */
	private class EvalNode{
		public final PhysicalOperator op;
		public EvalNode(PhysicalOperator op){
			this.op = op;
		}
	}

	/**
	 * Stack for evaluations
	 */
	private Stack<EvalNode> evalStack;

	public PhysicalPlanBuilder() {
		evalStack = new Stack<EvalNode>();

		if (!Main.setJoinConfig || !Main.setSortConfig){
			//Load physical plan from file
			Logger.println("Set up configuration from file.");
			File pplanfile = null;

			try {
				pplanfile = new File( SystemCatalogue.getSharedInstance().getInputPath() + "/plan_builder_config.txt" );
			} catch (IOException e) {
				e.printStackTrace();
			}
			PhysicalPlanBuilder.setConfig(pplanfile);	
		}		
	}
	
	/**
	 * 
	 * @param configFile
	 */
	public static void setConfig(File configFile){				
		FileInputStream fis = null;
		try {
			fis = new FileInputStream (configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner in = new Scanner( fis );
		Main.joinNum = in.nextInt();
		if( Main.joinNum == 1 ){
			Main.joinBuffers = in.nextInt();
		}
		Main.sortNum = in.nextInt();
		if( Main.sortNum == 1){
			Main.sortBuffers=in.nextInt();
		}
		in.close();
	}

	/**
	 * @return 	The physical operator corresponding to the result of t
	 * 			the physical planner
	 */
	public PhysicalOperator getPhysicalPlanResult(){
		EvalNode top = this.evalStack.pop();
		return top.op;
	}

	@Override
	public void visit(ScanOperator scan) {
		PhysicalOperator op = new operators.physical.ScanOperator(scan.getTable(), scan.getSchema());
		EvalNode node = new EvalNode(op);
		this.evalStack.push(node);
	}

	@Override
	public void visit(SelectOperator select) {
		select.getChild().accept(this);
		EvalNode node = this.evalStack.pop();
		PhysicalOperator op = new operators.physical.SelectionOperator(node.op, select.expr, select.baseTableNames);
		this.evalStack.push(new EvalNode(op));
	}

	@Override
	public void visit(ProjectionOperator projection) {
		projection.getChild().accept(this);
		EvalNode node = this.evalStack.pop();
		PhysicalOperator op = new operators.physical.ProjectionOperator(node.op, projection.cols);
		this.evalStack.push(new EvalNode(op));
	}

	@Override
	public void visit(JoinOperator join) {
		join.getLeftChild().accept(this);
		join.getRightChild().accept(this);

		EvalNode rightNode = this.evalStack.pop();
		EvalNode leftNode  = this.evalStack.pop();
		
		PhysicalOperator op;
		switch(Main.joinNum){
			case 0:{
				op = new SimpleJoinOperator(leftNode.op, rightNode.op, join.getExpression());
				break;
			}
			case 1:{
				op = new BNLJoinOperator(
						leftNode.op, 
						rightNode.op, 
						join.getExpression(), 
						Main.joinBuffers, 
						SystemCatalogue.PAGE_SIZE
				);
				break;
			}
			case 2:{
				//Sort Merge Join
				//TODO : Only can use SMJ in equi-join
				if (join.getLeftJoinColumn() != null) 
					Logger.println("Left Join Columns:"+Arrays.asList(join.getLeftJoinColumn()));
				if (join.getRightJoinColumn() != null)
					Logger.println("Right Join Columns:"+Arrays.asList(join.getRightJoinColumn()));
				operators.physical.SortOperator left_sort = new operators.physical.SortOperator(leftNode.op, join.getLeftJoinColumn());
				operators.physical.SortOperator right_sort = new operators.physical.SortOperator(rightNode.op, join.getRightJoinColumn());
				
				op = new SMJoinOperator(left_sort, right_sort, join.getExpression(), 
										join.getLeftJoinColumn(), join.getRightJoinColumn());
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid join number in conifiguration file line 1:"+Main.joinNum);
		}		
		this.evalStack.push(new EvalNode(op));
	}

	@Override
	public void visit(SortOperator sort) {
		sort.getChild().accept(this);
		EvalNode node = this.evalStack.pop();
		switch(Main.sortNum){
			case 0:{
				PhysicalOperator op = new operators.physical.SortOperator(node.op, sort.orderBy);
				this.evalStack.push(new EvalNode(op));
				break;
			}
			case 1:{
				PhysicalOperator op = 
						new operators.physical.ExternalSortOperator(node.op, sort.orderBy, Main.sortBuffers);
				this.evalStack.push(new EvalNode(op));
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid sort num in config line 2. Got:"+Main.sortNum);			
		}
	}

	@Override
	public void visit(DuplicateEliminateOperator dupElimOp) {
		dupElimOp.getChild().accept(this);
		EvalNode node = this.evalStack.pop();
		PhysicalOperator op = new operators.physical.DuplicateEliminationOperator(node.op);
		this.evalStack.push(new EvalNode(op));
	}

}
