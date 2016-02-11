package cs4321test;

import main.Logger;
import main.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.SystemCatalogue;

public class P2Provided {

	private final String INPUT_PATH 	= "test/p2Provided/input"; 
	private final String OUTPUT_PATH 	= "test/p2Provided/output";
	private final String ANSWER_PATH 	= "test/p2Provided/expected";
	private TestAux testAux = new TestAux();
	
	@Before
	public void setUp() throws Exception {
		// set up the system catalogue
		testAux = new TestAux();
		Logger.println("Running provided tests");
		SystemCatalogue.setSetInputFormat(".csv");
		Main.OUTPUT_BINARY_FORMAT = false;
		Main.INPUT_BINARY_FORMAT = false;
		Main.fullySorted = true;
	}

	@After
	public void tearDown() throws Exception {
		Main.reset();
	}

	@Test
	public void testSimple() {		
		// back to default
		testAux.joinConfig = new int[]{0};
		testAux.sortConfig = new int[]{0};

		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH,ANSWER_PATH);		
	}

	//@Test
	public void testWithExternalMergeSort(){	
		testAux.joinConfig = new int[]{0};
		testAux.sortConfig = new int[]{1,4};

		// back to default		
		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH+"_EMS",ANSWER_PATH);	
	}

	//@Test
	public void testWithBNLJ(){
		testAux.joinConfig = new int[]{1, 5};
		testAux.sortConfig = new int[]{0};

		// back to default		
		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH,ANSWER_PATH);
	}

	//@Test
	public void testWithSMJ(){
		testAux.joinConfig = new int[]{2};
		testAux.sortConfig = new int[]{0};

		// exclude tests contains non-equi join
		testAux.ignoreTestsArray.add("query22");
		testAux.ignoreTestsArray.add("query23");
		testAux.ignoreTestsArray.add("query24");

		// back to default		
		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH+"_SMJ",ANSWER_PATH);
	}

//	@Test
	public void testWithSMJWithEMS(){
		testAux.joinConfig = new int[]{2};
		testAux.sortConfig = new int[]{1,4};

		// exclude tests contains non-equi join
		testAux.ignoreTestsArray.add("query22");
		testAux.ignoreTestsArray.add("query23");
		testAux.ignoreTestsArray.add("query24");

		// back to default		
		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH+"_SMJ_EMS",ANSWER_PATH);
	}

}
