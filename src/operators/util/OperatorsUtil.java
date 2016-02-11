package operators.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import db.CustomColumn;
import db.Schema;
import db.SchemaWithDictionary;

public class OperatorsUtil {
	/**
	 * Glues two schemas together
	 * @return A schema containing ALL the columns from s1 and s2.
	 */
	public static Schema glueSchema(Schema s1, Schema s2){
		//Here just trying to merge two arrays
		CustomColumn[] arrOne=s1.getColumns();
		CustomColumn[] arrTwo=s2.getColumns();

		CustomColumn[] arrThree = new CustomColumn[arrTwo.length + arrOne.length];
		int index = arrOne.length;

		for (int i = 0; i < arrOne.length; i++) {
			arrThree[i] = arrOne[i];
		}
		for (int i = 0; i < arrTwo.length; i++) {
			arrThree[i + index] = arrTwo[i];    
		}

		return new SchemaWithDictionary(arrThree);
	}

	public static Expression oneEqualsToOne(){
		//Set default expression to 1=1
		LongValue l = new LongValue("1"); 
		EqualsTo eq = new EqualsTo();
		eq.setLeftExpression(l);
		eq.setRightExpression(l);
		return eq;
	}
}

