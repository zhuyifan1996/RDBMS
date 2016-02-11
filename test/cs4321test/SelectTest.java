package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;

import main.Logger;
import main.Main;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.schema.Table;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ScanOperator;
import operators.physical.SelectionOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.SystemCatalogue;


/**
 * Tests the functionality of Select Operator
 * @author Trevor Edwards
 *
 */
public class SelectTest {
	
	private final String INPUT_PATH 	= "test/select/input"; 
	private final String OUTPUT_PATH 	= "test/select/output";
	private final String ANSWER_PATH 	= "test/select/expected";
	private TestAux testAux = new TestAux();
	
	@Before
	public void setUp() throws Exception {
		Main.reset();
		SystemCatalogue.setSetInputFormat(".csv");
		Main.INPUT_BINARY_FORMAT = false;
		Main.OUTPUT_BINARY_FORMAT = false;
		Main.fullySorted = true;
		testAux = new TestAux();
		testAux.ignoreTestsArray.add("README");
	}

	@After
	public void tearDown() throws Exception {
		Main.reset();
	}
	
	@Test 
	public void resetTest() throws IOException{
		ScanOperator so2 = ScanTest.operatorFor("test/select/input/", "test/select/input/db/schema.txt", new Table("Sailors","Sailors"));
		SelectionOperator sel = new SelectionOperator( so2, new GreaterThanEquals(), new HashSet<String>());
		ScanOperator child = (ScanOperator) sel.getChild();
		PhysicalOperatorUtil.arrayDump(child);
		assertEquals( 0, PhysicalOperatorUtil.arrayDump(child).length );
		sel.reset();
		assertEquals( 6, PhysicalOperatorUtil.arrayDump(child).length );

	}

	@Test
	public void test() throws IOException {
		Logger.println("Running project test");
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
		
	}

}
