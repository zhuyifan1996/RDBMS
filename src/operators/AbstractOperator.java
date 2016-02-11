package operators;

import db.Schema;

public class AbstractOperator implements Operator {

	/**
	 * Each operator should have a single schema, which represents the format of the 
	 * tuple coming out.
	 */
	public Schema schema;
	
	/**
	 * TODO should we clone the schema
	 * @param schema
	 */
	public AbstractOperator(Schema schema) {
		this.schema = schema;
	}

	@Override
	public Schema getSchema() {
		return this.schema;
	}

}
