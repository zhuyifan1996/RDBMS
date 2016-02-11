package operators.physical;

import net.sf.jsqlparser.expression.Expression;
import operators.util.OperatorsUtil;
import db.Tuple;

/**
 * This class is an abstraction of the Join operator in relational Algebra
 * @author Guandao Yang, Alvin Zhu
 *
 */
public class SimpleJoinOperator extends BinaryPhysicalOperator {

	public Expression expression;
	
	// A copy of the left tuple currently being joined.
	//Its being null indicates that there is no more tuples left to be joined.
	protected Tuple currentLeftTuple;
	
	public SimpleJoinOperator(){super();}
	
	public SimpleJoinOperator(PhysicalOperator leftChild, PhysicalOperator rightChild, Expression exp) {
		super(leftChild, rightChild);
//		System.out.println("[In TNLJ]"+this.rightChild);
		this.expression=exp;
		this.schema = OperatorsUtil.glueSchema(leftChild.getSchema(),rightChild.getSchema());
		this.currentLeftTuple=leftChild.getNextTuple();

	}

	@Override
	public void reset() {
		this.leftChild.reset();
		this.rightChild.reset();
		this.currentLeftTuple=leftChild.getNextTuple();
	}

	/**
	 * If currentLeftTuple is null, return null. Then call right.getNextTuple recursively until get a desired tuple.
	 * In the process call left.getNextTuple when needed.
	 * @return The next joined tuple which satisfies this.expression
	 */
	@Override
	public Tuple getNextTuple() {
		while( true ){
			if (this.currentLeftTuple==null) return null;		
			Tuple t2;
			if ((t2=this.rightChild.getNextTuple())!=null){
				DualExpressionEvaluator evaluator = new DualExpressionEvaluator(this.currentLeftTuple,t2);
				this.expression.accept(evaluator);
				if (evaluator.getResult()){
					return this.glueTuple(this.currentLeftTuple, t2);
				}
			}else{
				this.rightChild.reset();
				this.currentLeftTuple=this.leftChild.getNextTuple();
			}
		}
	}

	/** Glue two tuples together with the schema in this join operator. Tuple order matters.
	 * First tuple must be from left child and second tuple must be from right child.
	 * @return A glued tuple
	 */
	protected Tuple glueTuple(Tuple t1,Tuple t2){
		Object[] data1=t1.getData();
		Object[] data2=t2.getData();
		Object[] dataglued = new Object[data1.length+data2.length];
		int index=data1.length;
		
		for (int i = 0; i < data1.length; i++) {
		    dataglued[i] = data1[i];
		}
		for (int i = 0; i < data2.length; i++) {
		    dataglued[i + index] = data2[i];    
		}
		
		return new Tuple(this.schema, dataglued);
	}
	
	@Override
	public String toString(){
		return "SimpleJoin(" + this.leftChild.toString()+", " +this.expression+", "+this.rightChild.toString() +")"; 
	}
}
