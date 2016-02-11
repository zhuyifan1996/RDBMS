package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import main.Logger;
import main.Main;
import net.sf.jsqlparser.schema.Table;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ProjectionOperator;
import operators.physical.ScanOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.CustomColumn;
import db.SystemCatalogue;
/**
 * Tests the methods of our projection operator
 * @author Trevor Edwards
 *
 */
public class ProjectTest {
	
	private final String INPUT_PATH 	= "test/project/input"; 
	private final String OUTPUT_PATH 	= "test/project/output";
	private final String ANSWER_PATH 	= "test/project/expected";
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
		Logger.println("Running project test");
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);		
	}
	
	@Test
	public void resetTest() throws IOException{
		Table tab = new Table("Sailors","Sailors");
		ScanOperator so = ScanTest.operatorFor("test/distinct/input/","test/distinct/input/db/schema.txt",tab);
		
		//Order by the columns and dump, reset, dump
		ProjectionOperator d1;
		int l1 = -1;
		int l2 = -1;
			d1 = new ProjectionOperator( so, new CustomColumn[0] );
			//This will not remove duplicates as it is not sorted, but still tests reset well
			PhysicalOperatorUtil.dump(so);
			l1 = PhysicalOperatorUtil.arrayDump(so).length;
			d1.reset();
			
			l2= PhysicalOperatorUtil.arrayDump(so).length;
			assertTrue(l1 == 0);
			assertTrue( l2 == 6 );
	}

}
