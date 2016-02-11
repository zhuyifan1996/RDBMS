package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import main.Logger;
import main.Main;
import net.sf.jsqlparser.schema.Table;
import operators.physical.DuplicateEliminationOperator;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ScanOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.SystemCatalogue;


public class DistinctTest {

	private final String INPUT_PATH 	= "test/distinct/input"; 
	private final String OUTPUT_PATH 	= "test/distinct/output";
	private final String ANSWER_PATH 	= "test/distinct/expected";
	private TestAux testAux = new TestAux();
	
	@Before
	public void setUp() throws Exception {
		Main.reset();
		SystemCatalogue.setSetInputFormat(".csv");
		Main.INPUT_BINARY_FORMAT = false;
		Main.OUTPUT_BINARY_FORMAT = false;
		Main.fullySorted = true;
		testAux.ignoreTestsArray.add("README");
	}

	@After
	public void tearDown() throws Exception {
		Main.reset();
	}
	
	@Test
	public void test() throws IOException {
		Logger.println("Running distinct test");
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
	}
	
	@Test
	public void resetTest() throws IOException{
		//This will fail with the -ea flag, don't worry about it.
		Table tab = new Table("Sailors","Sailors");
		ScanOperator so = ScanTest.operatorFor("test/distinct/input/","test/distinct/input/db/schema.txt",tab);
		
		//Order by the columns and dump, reset, dump
		DuplicateEliminationOperator d1;
		int l1 = -1;
		int l2 = -1;
			d1 = new DuplicateEliminationOperator( so );
			//This will not remove duplicates as it is not sorted, but still tests reset well
			PhysicalOperatorUtil.arrayDump(so);

			PhysicalOperatorUtil.dump(so);
			l1 = PhysicalOperatorUtil.arrayDump(so).length;
			d1.reset();
			l2 = PhysicalOperatorUtil.arrayDump(so).length;
			assertTrue(l1 == 0);
			assertTrue( l2 == 6 );
	}


}
