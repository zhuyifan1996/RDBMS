package operators.physical;

import db.CustomColumn;
import db.Schema;
import db.Tuple;

/**
 * Operator for projection. Child must not be null. 
 * @author Alvin Zhu
 *
 */
public class ProjectionOperator extends UnaryPhysicalOperator {
	
	private Schema schema;
	private CustomColumn[] remainingCols;
	
	public ProjectionOperator(PhysicalOperator child, CustomColumn[] cols) {
		super(child);
		
		this.remainingCols = cols.clone();
		this.schema = child.getSchema().subSchema(this.remainingCols);
	}

	@Override
	public Schema getSchema(){
		return this.schema;
	}
	
	@Override
	public void reset() {
		this.child.reset();
	}

	@Override
	public Tuple getNextTuple() {
		Tuple t;
		if ((t = this.child.getNextTuple())!=null){
			Object[] data= this.subTuple(t, this.remainingCols);
			try {
				return new Tuple(this.schema,data);
			}catch(Exception e){
				e.printStackTrace(System.err);
			}
		}
		return null;
	}
	
	/**
	 * @param parentTuple: The tuple to get the data from
	 * @param cols: The cols we need
	 * @return The data of the subTuple corresponding to cols
	 */
	private Object[] subTuple(Tuple parentTuple, CustomColumn[] cols){
		Object[] output = new Object[cols.length];
		for (int i=0;i<cols.length;i++){
			output[i]=parentTuple.getDataForColumn(cols[i]);
		}
		return output;
	}

	@Override
	public String toString(){
		String cols = "[";
		for (CustomColumn c : this.remainingCols){
			cols += c.getTable().getName()+"."+c.getName()+", ";
		}
		cols+="]";
		
		return "PhysicalProjection("+this.child +", "+cols+")";
	}

}
