package operators.physical;

import db.CustomColumn;
import db.DATA_TYPE;
import db.Tuple;

import net.sf.jsqlparser.schema.Column;

/**
 * The only place that uses tp in SingleExpressionEvaluator is in visit(Column)
 * So we can extend that class.
 *
 */
public class DualExpressionEvaluator extends SingleExpressionEvaluator {

	/**
	 * Tuples on which we evaluate 
	 */
	private Tuple leftTp;
	private Tuple rightTp;
		
	public DualExpressionEvaluator(Tuple l, Tuple r) {
		super();
		this.leftTp = l;
		this.rightTp = r;
	}

	@Override
	public void visit(Column arg0) {
		CustomColumn c = new CustomColumn(arg0);

		Object objL = this.leftTp.getDataForColumn(c);
		DATA_TYPE typeL = this.leftTp.getTypeForColumn(c);
		Object objR = this.rightTp.getDataForColumn(c);
		DATA_TYPE typeR = this.rightTp.getTypeForColumn(c);

		Object obj     = objL != null ? objL : objR;
		DATA_TYPE type = objL != null ? typeL : typeR;

		this.evalStack.push(new EvalResult(obj, type));

	}
	
}
