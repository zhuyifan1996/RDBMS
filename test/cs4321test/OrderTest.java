package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import main.Logger;
import main.Main;
import net.sf.jsqlparser.schema.Table;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ScanOperator;
import operators.physical.SortOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.SystemCatalogue;


/**
 * Tests the function of SortOperator
 * @author Trevor Edwards
 *
 */
public class OrderTest {

	private final String INPUT_PATH 	= "test/order/input"; 
	private final String OUTPUT_PATH 	= "test/order/output";
	private final String ANSWER_PATH 	= "test/order/expected";
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
	public void test() throws IOException {
		Logger.println("Running order test");
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
		
	}
	
	@Test
	public void resetTest() throws IOException{
		Table tab = new Table("Sailors","Sailors");
		ScanOperator so = ScanTest.operatorFor("test/order/input/","test/order/input/db/schema.txt",tab);

		
		//Order by the columns and dump, reset, dump
		SortOperator j1 = new SortOperator( so, so.getSchema().getColumns());
		PhysicalOperatorUtil.dump(j1);
		assertTrue( PhysicalOperatorUtil.arrayDump(j1).length == 0 );
		j1.reset();
		assertTrue( PhysicalOperatorUtil.arrayDump(j1).length == 6 );
		
	}

}
