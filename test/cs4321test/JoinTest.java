package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import main.Logger;
import main.Main;
import net.sf.jsqlparser.schema.Table;
import operators.physical.SimpleJoinOperator;
import operators.util.OperatorsUtil;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ScanOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import db.SystemCatalogue;



/**
 * Tests the functionality of join
 * @author Trevor Edwards
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JoinTest {

	private final String INPUT_PATH 	= "test/join/input"; 
	private final String OUTPUT_PATH 	= "test/join/output";
	private final String ANSWER_PATH 	= "test/join/expected";
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
	public void test() throws IOException, InterruptedException {
		Logger.println("Running join test");		
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
	}
	
	@Test
	public void resetTest() throws IOException{
		Table tab = new Table("Sailors","Sailors");
		ScanOperator so = ScanTest.operatorFor("test/join/input/","test/join/input/db/schema.txt",tab);
		ScanOperator so2 = ScanTest.operatorFor("test/join/input/","test/join/input/db/schema.txt",tab);
		
		
		//Since we are joining the same tables, we expect 6 entries
		SimpleJoinOperator j1 = new SimpleJoinOperator( so, so2, OperatorsUtil.oneEqualsToOne());
		ScanOperator baby = (ScanOperator) j1.getLeftChild();
		PhysicalOperatorUtil.dump(baby);
		assertEquals(0, PhysicalOperatorUtil.arrayDump(baby).length);
		j1.reset();
		
		//This is five because of the way we implemented join
		assertEquals(5, PhysicalOperatorUtil.arrayDump(baby).length);
		
	}

}
