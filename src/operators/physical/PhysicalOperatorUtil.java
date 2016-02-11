package operators.physical;

import java.util.ArrayList;

import main.Logger;
import db.Tuple;

/**
 * Class that provides static helper methods
 * @author Guandao Yang
 *
 */
public class PhysicalOperatorUtil {
	
	/**
	 * Prints all tuples from this operator
	 */
	public static void dump(PhysicalOperator op) {
		while( true ){
			Tuple tup = op.getNextTuple();
			if( tup == null) break;
			Logger.println("Dumping results of an operator");
			Logger.println(tup);
			Logger.println("End of dump" );
		}

	}

	/**
	 * Return all tuples from this operator
	 * This will not be scalable, some table might not fit in memory
	 */
	public static Tuple[] arrayDump(PhysicalOperator op) {
		ArrayList<Tuple> tuppies = new ArrayList<Tuple>();
		while( true ){
			Tuple tup = op.getNextTuple();
			if( tup == null) break;
			tuppies.add(tup);
		}
		//Fixes a typing issue, but a bit hacky.
		return tuppies.toArray( new Tuple[0]);
	}
	
}
