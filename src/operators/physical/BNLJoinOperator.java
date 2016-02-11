package operators.physical;

import java.util.ArrayList;

import db.Tuple;
import main.Logger;
import net.sf.jsqlparser.expression.Expression;
import operators.util.OperatorsUtil;

/**
 * Implementation of Block Nested Loop Join
 * @author Trevor Edwards
 *
 */
public class BNLJoinOperator extends BinaryPhysicalOperator {
	
public final Expression expression;
	
	private int tupleSize;
	private int buffers;
	private ArrayList<Tuple[]> bufferArray;
	private int writeHead;
	private int readHead;
	private int maxWrite;
	private Tuple checkTuple;
	
	public BNLJoinOperator(PhysicalOperator leftChild, PhysicalOperator rightChild, Expression exp, int buffers, int bufferSize) {
		super(leftChild, rightChild);
		this.expression=exp;
		this.schema = OperatorsUtil.glueSchema(leftChild.getSchema(),rightChild.getSchema());
		tupleSize = bufferSize / (4 * leftChild.getSchema().getNumberOfColumns());
		this.buffers = buffers;
		refreshBufferArray();
		writeHead = 0;
		maxWrite = tupleSize * buffers;
		readHead = 0;
		Logger.println("Block Nested Loop Join Operator Initialized");
	}
	
	/**
	 * Resets the buffer array with new blocks
	 */
	private void refreshBufferArray(){
		bufferArray = new ArrayList<>();
		for( int i = 0; i < buffers; i++ ){
			bufferArray.add( new Tuple[tupleSize]);
		}
		
	}

	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();
		refreshBufferArray();
	}
	
	/**
	 * Called when the outer block has been processed
	 */
	private void resetInner(){
		writeHead = 0;
		readHead = 0;
		rightChild.reset();
		refreshBufferArray();
	}

	@Override
	public Tuple getNextTuple() {
			//Check for tuples in outer block
		while( true ){
			Tuple leftNext;
			while( writeHead < maxWrite && (leftNext = leftChild.getNextTuple()) != null){
				//Fill up the next block
				bufferArray.get( writeHead / tupleSize )[writeHead % tupleSize] = leftNext;
				writeHead++;
			}
			
			if( writeHead == 0 ) return null;
			
			if( checkTuple != null && readHead < writeHead ){
				//There is an inner tuple to scan, so scan over the block
				Tuple outerTup = bufferArray.get( readHead / tupleSize )[readHead % tupleSize];
				readHead++;
				
				DualExpressionEvaluator evaluator = new DualExpressionEvaluator(outerTup, checkTuple);
				expression.accept(evaluator);
				
				if (evaluator.getResult())
					return this.glueTuple(outerTup, checkTuple); 
				
			} else{
				//get a new inner tuple
				readHead = 0;
				checkTuple = rightChild.getNextTuple();
				if( checkTuple == null)
					resetInner();
			}
		}
	}

	/** Glue two tuples together with the schema in this join operator. Tuple order matters.
	 * First tuple must be from left child and second tuple must be from right child.
	 * @return A glued tuple
	 */
	private Tuple glueTuple(Tuple t1,Tuple t2){
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
		return "BNLJ[BUFFERS: "+ buffers+", TUPLESIZE: "+ tupleSize +"](" + this.leftChild.toString()+", " +this.expression+", "+this.rightChild.toString() +")"; 
	}

}
