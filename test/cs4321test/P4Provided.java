package cs4321test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import db.NIODataConverter;
import main.Logger;
import main.Main;

/**
 * The given test cases for p4
 * @author Trevor Edwards
 *
 */
public class P4Provided {
	

	private final String CONFIG_PATH 	= "test/p4provided/interpreter_config_file.txt";
	private final String ANSWER_PATH 	= "test/p4provided/expected_indexes";
	private final String OUTPUT_PATH =    "test/p4provided/output";
	private final String EXPECTED_PATH =    "test/p4provided/expected";
	private final String INDEX_PATH     = "indexes";

	@Test
	public void test() {
		Logger.println("Running p4 tests");
		//Custom index test:
		
		// directory cleanup
		Main.cleanUpTmpDirRecursively(INDEX_PATH);
		File dir = new File(INDEX_PATH);
		if(dir.list() == null) return;
		for (String file:dir.list()){
			File newFile = new File(dir.getAbsolutePath()+"/"+file);
			newFile.delete();
		}	

		try{
			Main.main(new String[] {CONFIG_PATH});	
		}catch (Exception e){
			fail(e.getMessage());
		}
		
		TestAux helper = new TestAux();
		
		//compare index results
		helper.compareBinaryOutput(INDEX_PATH, ANSWER_PATH);
		
		//compare query results TODO verify these
		helper.compareBinaryOutput(OUTPUT_PATH, EXPECTED_PATH);
		
		
	}

}
