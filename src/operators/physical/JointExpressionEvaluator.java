package operators.physical;

import db.CustomColumn;
import db.DATA_TYPE;
import db.Tuple;

import net.sf.jsqlparser.schema.Column;

/**
 * This will evaluate the expression for joint operations
 * @author Guandao Yang
 *
 */
public class JointExpressionEvaluator extends SingleExpressionEvaluator {

	/**
	 * The tuples from which this expression is evaluated
	 */
	private Tuple tpLeft;
	private Tuple tpRight;
	
	public JointExpressionEvaluator(Tuple tpLeft, Tuple tpRight) {
		// TODO: For join, we required to refer to table name?
		super(null, null);
		this.tpLeft = tpLeft;
		this.tpRight = tpRight;
	}

	
	@Override
	public void visit(Column arg0) {
		CustomColumn c = new CustomColumn(arg0);
		Object data;
		DATA_TYPE type;
		
		data = this.tpLeft.getDataForColumn(c);
		if (data != null){
			type = this.tpLeft.getTypeForColumn(c);	
		}else{
			data = this.tpRight.getDataForColumn(c);
			type = this.tpRight.getTypeForColumn(c);	
		}
		
		this.evalStack.push(new EvalResult(data, type));
	}

}
