package cs4321test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import main.Logger;
import main.Main;
import operators.util.ExternalMergeSort;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.NIODataConverter;

public class P3Provided {

	private final String INPUT_PATH 	= "test/graded/input"; 
	private final String OUTPUT_PATH 	= "test/graded/output";
	private final String ANSWER_PATH 	= "test/graded/expected";
	private final String SORTED_ANSWER_PATH = "test/graded/expected_sorted";
	private final String SYSTEM_TMP_PATH = "tmp/";
	private TestAux testAux = new TestAux();

	@Before
	public void setUp() throws Exception {
		getAnswerReady();
		Main.reset();
		Main.OUTPUT_BINARY_FORMAT = true;
		Main.INPUT_BINARY_FORMAT = true;
		testAux = new TestAux();
		testAux.useThisConfig = false;
		testAux.sortedTheResult = true;
		testAux.ignoreTestsArray.add("README");

	}

	@After
	public void tearDown() throws Exception {
		Main.reset();
	}

	/**
	 * Will sort all the expected file and get them ready
	 */
	public void getAnswerReady(){
		File answerFile = new File(this.ANSWER_PATH);

		for(String fileName:answerFile.list()){
			if (!fileName.contains("_humanreadable") && !fileName.contains(".DS_Store")){

				File destination = new File(SORTED_ANSWER_PATH+"/"+fileName);
				File toBeSorted = new File(this.ANSWER_PATH+"/"+fileName);
				// then use the exernal merge sort to sort the tuples
				ExternalMergeSort ems = new ExternalMergeSort(toBeSorted, true, SYSTEM_TMP_PATH, null);
				try {
					File sorted = ems.getSortedFile();
					if (sorted != null){
						sorted.renameTo(destination);	
					}else{
						Logger.warnln("Sorting "+fileName+" gets "+sorted);
					}

				} catch (IllegalArgumentException | NoSuchAlgorithmException
						| IOException e) {
					Logger.warnln("Fail to sort file"+fileName);
					e.printStackTrace();
				}

			}
		}
	}

	@Test
	public void test() {
		Logger.println("Running p3 tests");
		testAux.testGeneric(INPUT_PATH,OUTPUT_PATH,SORTED_ANSWER_PATH);
		try {
			File outDir = new File(OUTPUT_PATH+"_convert");
			outDir.mkdirs();

			File sortedAnsPath = new File(SORTED_ANSWER_PATH+"_convert");
			sortedAnsPath.mkdirs();
			
			NIODataConverter.main(new String[]{OUTPUT_PATH, OUTPUT_PATH+"_convert/","b"});
			NIODataConverter.main(new String[]{SORTED_ANSWER_PATH, SORTED_ANSWER_PATH+"_convert/","b"});
		} catch (IllegalArgumentException | IOException e) {			
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
