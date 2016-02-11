package cs4321test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import db.Tuple;
import db.TupleReader;
import main.Logger;
import main.Main;
import operators.util.ExternalMergeSort;

public class TestAux {

	private final String SYSTEM_TMP_PATH = "tmp/";
	
	public boolean useThisConfig = false;
	public int[] joinConfig = new int[]{0};
	public int[] sortConfig = new int[]{0};
	public boolean sortedTheResult = false;

	// Contain a list of files that we should ignore
	public ArrayList<String> ignoreTestsArray = new ArrayList<String>();		

	/**
	 * Run Main on INPUT_PATH, and write it to OUTPUT_PATH, and compare each
	 * file in the OUTPUT_PATH to the ANSWER_PATH
	 * @param INPUT_PATH
	 * @param OUTPUT_PATH
	 * @param ANSWER_PATH
	 */
	public void testGeneric( String[] args, String ANSWER_PATH, String OUTPUT_PATH){

		if (this.useThisConfig){
			Main.setJoinOptions(this.joinConfig);
			Main.setSortOptions(this.sortConfig);			
		}
		
		// clean up the output directory 
		Main.cleanUpTmpDirRecursively(OUTPUT_PATH);
		
		try{
			Main.main(args);	
		}catch (Exception e){
			fail(e.getMessage());
		}
		

		if (sortedTheResult){
			// will need to sorte this result again
			sortTheOutput(OUTPUT_PATH);
		}

		if (Main.OUTPUT_BINARY_FORMAT){
			compareBinaryOutput(OUTPUT_PATH, ANSWER_PATH);
		}else{
			compareNoneBinaryOutput(OUTPUT_PATH, ANSWER_PATH);
		}

	}
	
	public void testGeneric( String INPUT_PATH, String OUTPUT_PATH, String ANSWER_PATH){
		testGeneric( new String[] {INPUT_PATH,OUTPUT_PATH}, ANSWER_PATH, OUTPUT_PATH);
	}
	
	public void testConfigGeneric( String configpath, String outputpath, String ANSWER_PATH){
		testGeneric( new String[] {configpath}, ANSWER_PATH, outputpath);
	}

	/**
	 * Whether we should sorted the output before comparing them.
	 * @param outputPath
	 */
	private void sortTheOutput(String outputPath){
		File inputFile = new File(outputPath);

		for(String fileName:inputFile.list()){
			if (!fileName.contains("_humanreadable")){
				File destination = new File(outputPath+"/"+fileName);
				File toBeSorted = new File(outputPath+"/"+fileName);
				// then use the exernal merge sort to sort the tuples
				ExternalMergeSort ems = new ExternalMergeSort(toBeSorted, Main.OUTPUT_BINARY_FORMAT, SYSTEM_TMP_PATH, null);
				try {
					File sorted = ems.getSortedFile();
					sorted.renameTo(destination);
				} catch (IllegalArgumentException | NoSuchAlgorithmException
						| IOException e) {
					Logger.warnln("Fail to sort file"+fileName);
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * 
	 * @param OUTPUT_PATH
	 * @param ANSWER_PATH
	 */
	public void compareBinaryOutput(String OUTPUT_PATH, String ANSWER_PATH){
		String errorMessage = "\n";
		boolean pass = true;

		File outFolder = new File(OUTPUT_PATH);
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(outFolder.list()));

		// will use buffer tuple reader
		int totalLoops = 0;
		fileLoop:for (String name : names){
			totalLoops++;
			
			if (this.ignoreTestsArray.contains(name)){
				continue fileLoop;
			}

			Logger.println("Running test on " + name);
			File outFile = new File(OUTPUT_PATH+"/"+name);
			File ansFile = new File(ANSWER_PATH+"/"+name);

			TupleReader readerOut = Main.tupleReaderFactory(outFile, null);
			TupleReader readerAns = Main.tupleReaderFactory(ansFile, null);

			int count = 0;
			while (readerOut.hasNextTuple() && readerAns.hasNextTuple()){
				count++;
				Tuple outTp = readerOut.getNextTuple();
				Tuple ansTp = readerAns.getNextTuple();
				if (!outTp.equals(ansTp)){
					pass = false;
					errorMessage = errorMessage + "File:"+name+"; Tuple:"+count +":Unequal outputs in file \n Saw: "+outTp+" \n Wanted: " + ansTp;
					pass = false;
					continue fileLoop;
				}
			}

			if (readerOut.hasNextTuple()){
				errorMessage = errorMessage + "File:"+name+"; Line:"+count +":Extra rows outputted in file \n";
				pass = false;
				continue fileLoop;
			}

			if (readerAns.hasNextTuple()){
				errorMessage = errorMessage + "File:"+name+"; Line:"+count +":Not enough rows outputted in file \n";
				pass = false;
				continue fileLoop;
			}

			readerOut.close();
			readerAns.close();

		}

		if (totalLoops == 0){
			errorMessage = errorMessage + "No output file.";
			Logger.warnln(errorMessage);
			fail(errorMessage);
		}
		
		if (!pass){
			Logger.warnln(errorMessage);
			fail(errorMessage);
			return;
		}
	}

	/**
	 * 
	 * @param OUTPUT_PATH
	 * @param ANSWER_PATH
	 */
	public void compareNoneBinaryOutput(String OUTPUT_PATH, String ANSWER_PATH){
		String errorMessage = "\n";
		boolean pass = true;

		File outFolder = new File(OUTPUT_PATH);
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(outFolder.list()));

		int totalLoops = 0;
		fileLoop:for (String name : names){
			totalLoops++;
			
			if (this.ignoreTestsArray.contains(name)){
				continue fileLoop;
			}

			try {
				Logger.println("Running test on " + name);
				File out = new File(OUTPUT_PATH+"/"+name);
				Scanner scanOut = new Scanner(out);

				File ans = new File(ANSWER_PATH+"/"+name);
				Scanner scanAns = new Scanner(ans);

				int count = 0;
				while (scanOut.hasNextLine() && scanAns.hasNextLine()){
					count++;
					String outStr = scanOut.nextLine();
					String ansStr = scanAns.nextLine();
					if (!outStr.equals(ansStr)){
						pass = false;
						errorMessage = errorMessage + "File:"+name+"; Line:"+count +":Unequal outputs in file \n Saw: "+outStr+" \n Wanted: " + ansStr;
						pass = false;
						continue fileLoop;
					}
				}

				if (scanOut.hasNextLine()){
					errorMessage = errorMessage + "File:"+name+"; Line:"+count +":Extra rows outputted in file \n";
					pass = false;
					continue fileLoop;
				}

				if (scanAns.hasNextLine()){
					errorMessage = errorMessage + "File:"+name+"; Line:"+count +":Not enough rows outputted in file \n";
					pass = false;
					continue fileLoop;
				}

				scanOut.close();
				scanAns.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fail("Exception encountered in file "+name);
			}

		}
		
		if (totalLoops == 0){
			errorMessage = errorMessage + "No output file.";
			Logger.warnln(errorMessage);
			fail(errorMessage);
		}
		
		if (!pass){
			Logger.warnln(errorMessage);
			fail(errorMessage);
		}

	}

}
