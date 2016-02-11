package cs4321test;

import org.junit.Test;

import main.Logger;

/**
 * Runs our tests to compare join algorithms
 * @author Trevor Edwards
 *
 */
public class Experiment {

	private TestAux testAux = new TestAux();

	//To do this properly, comment out other tests when running
	//	@Test
	//	public void bnljrun(){
	//		String INPUT_PATH 	= "experiment/bnlj/input"; 
	//		String OUTPUT_PATH 	= "experiment/bnlj/output";
	//		String ANSWER_PATH 	= "experiment/bnlj/expected";
	//		
	//		Logger.println("Running bnlj experiment");
	//		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
	//		
	//	}

	//@Test
	//	public void tnljrun(){
	//		String INPUT_PATH 	= "experiment/tnlj/input"; 
	//		String OUTPUT_PATH 	= "experiment/tnlj/output";
	//		String ANSWER_PATH 	= "experiment/tnlj/expected";
	//		
	//		Logger.println("Running tnlj experiment");
	//		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);
	//		
	//	}
	//	
	@Test
	public void smjrun(){
		String INPUT_PATH 	= "experiment/smj/input"; 
		String OUTPUT_PATH 	= "experiment/smj/output";
		String ANSWER_PATH 	= "experiment/smj/expected";

		Logger.println("Running smj experiment");
		testAux.testGeneric(INPUT_PATH, OUTPUT_PATH, ANSWER_PATH);

	}

}	
