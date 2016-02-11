package operators.physical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import db.CustomColumn;
import db.Tuple;
import main.Logger;
import main.Main;

/**
 * Sorts the result of a query by placing values into a buffer and using Collection's sort functionality.
 * TODO: Ensure this is placed at the top of the evaluation tree
 * @author Trevor Edwards
 *
 */
public class SortOperator extends UnaryPhysicalOperator {

	private ArrayList<Tuple> sortedTuples;
	private int head;
	private ArrayList<CustomColumn> orderBy;

	/**
	 * The comparator of the tuple specified by the [orderBy] columns
	 */
	protected Comparator<Tuple> comparator;

	// flag on whether we have perform the sort
	private boolean hasSorted = false;
		
	/**
	 * Constructs a sort operator
	 * @param child The child of this operator
	 * @param orderBy The columns stated in the ORDER BY clause
	 */
	public SortOperator(PhysicalOperator child, CustomColumn[] orderBy) {
		super(child);
		if( orderBy != null){
			this.orderBy = new ArrayList<>(Arrays.asList(orderBy));
		} else {
			this.orderBy = new ArrayList<>(Arrays.asList(this.getSchema().getColumns()));
			Logger.println("orderBy was null, override into:"+this.orderBy);
		}
		head = 0;

		// create the comparator	
		
		// if required to be fully sorted, then
		// we need to pass every column as a prerequisite for sorting
		if (Main.fullySorted){
			CustomColumn[] cc = child.getSchema().getColumns(); //Assumes that these are sorted as in schema
			for( CustomColumn c : cc){
				//Check if the column is in the array, if not append it
				if( !this.orderBy.contains( c )){
					this.orderBy.add( c );
				}
			}	
		}
		
		final CustomColumn[] colArr = this.orderBy.toArray(new CustomColumn[1]);
		this.comparator = new Comparator<Tuple>(){
			@Override
			public int compare(Tuple t1, Tuple t2) {
				if (t1 != null) return t1.customCompare(colArr, t2);
				if (t2 != null) return t2.customCompare(colArr, t1);
				Logger.warnln("BOTH T1 and T2 is NULL"+t1+","+t2);
				return 0;
			}
		};
	}

	@Override
	public void reset() {
		child.reset();
		head = 0;
	}

	/**
	 * Reset the operator,
	 * so that the next call of getNextTuple will return the tuple at [index]
	 * in the result relation.
	 */
	public void reset(int index){
		this.head = index;
	}

	/**
	 * Sorts tuples first by the specified attributes, then by the remaining ones
	 * Sort is ascending
	 */
	private void sort(){
		//Fetch all of our tuples to be sorted
		sortedTuples = new ArrayList<Tuple>( Arrays.asList(PhysicalOperatorUtil.arrayDump(this.child)) );

		//Sort
		Collections.sort(sortedTuples, this.comparator);
	}

	@Override
	public Tuple getNextTuple() {
		if (!this.hasSorted){			
			sort();
			this.hasSorted = true;
		}
		
		if (head < sortedTuples.size()){
			Tuple ret = sortedTuples.get(head);
			head++;
			return ret;
		}
		else
			return null;
	}

	@Override
	public String toString(){
		return "PhysicalSort("+this.orderBy+", "+this.child+")";
	}
}
