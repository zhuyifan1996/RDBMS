package operators.physical;

import main.Logger;
import net.sf.jsqlparser.expression.Expression;
import db.CustomColumn;
import db.Tuple;

/**
 * Implemented Sort Merge Join. 
 * @author Alvin Zhu
 *
 */
public class SMJoinOperator extends SimpleJoinOperator {

	public Expression expression;

	/** Counter indicating the index of the first inner relation
	 * tuple that is equal to current processing tuple on join attribute.*/
	private int head;

	/** Set of join attributes of this join operator. */
	private CustomColumn[] left_compare_cc;
	private CustomColumn[] right_compare_cc;

	private SortOperator leftChild;
	private SortOperator rightChild;

	//flag indicating if this is doing a cross-product
	final boolean is_cross_product;

	/**Assumes that in planner, already made leftChild and rightChild both some kind of 
	 * sort operator.
	 * */
	public SMJoinOperator(SortOperator leftChild, SortOperator rightChild, 
			Expression exp, CustomColumn[] left_cc, CustomColumn[] right_cc){
		super(leftChild,rightChild,exp);
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.expression = exp;
		head = 0;
		this.left_compare_cc=left_cc;
		this.right_compare_cc=right_cc;

		//Handle the case for cross product
		//TODO: if this is a cross-product, might be a bad idea to use SMJ
		if (this.left_compare_cc == null || this.right_compare_cc == null){
			if (!(this.left_compare_cc == null && this.right_compare_cc == null))
				System.err.println("Inconsistent join columns passed to SMJ");
//			Logger.warnln("SMJ set to Cross Product");
			//Set the flag
			is_cross_product = true;

		}else{
			is_cross_product = false;
		}
	}

	@Override
	public void reset() {
		this.leftChild.reset();
		this.rightChild.reset();
		head = 0;
	}

	@Override 
	public Tuple getNextTuple(){
		if(this.is_cross_product){
			return getNextTuple_cross_product();
		}else{
			return this.getNextTuple_normal();
		}
	}

	private Tuple getNextTuple_cross_product(){
		if (this.currentLeftTuple==null) return null;		
		Tuple t2;
		if ((t2=this.rightChild.getNextTuple())!=null){
			return this.glueTuple(this.currentLeftTuple, t2);
		}else{
			this.rightChild.reset();
			this.currentLeftTuple=this.leftChild.getNextTuple();
			return this.getNextTuple();
		}
	}

	private Tuple getNextTuple_normal() {
		if (this.currentLeftTuple==null) return null;	
		Tuple t2=this.rightChild.getNextTuple();

		if (t2 != null){
			int compareResult = 0;
			for (int i = 0; i < left_compare_cc.length; i++){				
				int tmp = this.currentLeftTuple.columnwiseCompare(left_compare_cc[i], right_compare_cc[i], t2);
				if (tmp != 0) compareResult = tmp;					
			}

			if(compareResult<0){
				//Roll back the right child to first of this partition
				this.rightChild.reset(head);
				//Proceed t1 and continue
				this.currentLeftTuple = this.leftChild.getNextTuple();
				return getNextTuple();
			}else if(compareResult>0){
				//Proceed t2 and continue
				head++;
				return getNextTuple();
			}else{
				//The join condition is met
				return this.glueTuple(this.currentLeftTuple, t2);
			}

		}else{
			//Roll back the right child to first of this partition
			this.rightChild.reset(head);
			//Proceed t1 and continue
			this.currentLeftTuple = this.leftChild.getNextTuple();
			return getNextTuple();
		}
	}
	
	@Override
	public String toString(){
		return "SortMergeJoin"+super.toString().substring(10);
	}

}
