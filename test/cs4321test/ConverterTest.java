package cs4321test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;

import db.NIODataConverter;
import main.Logger;

/**
 * Confirms that our data-type converter works
 * @author Trevor Edwards
 *
 */
public class ConverterTest {
	
	//Byte-human-byte
	
	//Human-byte-human
	@Test
	public void byteToHumanTest() throws IllegalArgumentException, IOException{
		//This also fails if run in AllTests, don't worry about it
		String INPUT_FILE = "test/converter/in/samp";
		String OUTPUT_TEMP_FILE = "test/converter/temp/temp";
		String OUTPUT_FILE = "test/converter/out";

		//convert a human file to byte
		NIODataConverter.humanToByte(INPUT_FILE, OUTPUT_TEMP_FILE);
		
		//convert back to human
		NIODataConverter.byteToHuman(OUTPUT_TEMP_FILE, OUTPUT_FILE);
		
		//iterate through lines for equality
		Logger.println("Running conversion test");
		File out = new File(OUTPUT_FILE);
		Scanner scanOut = new Scanner(out);
		
		File ans = new File(INPUT_FILE);
		Scanner scanAns = new Scanner(ans);
		
		String errorMessage = "Failed:\n";
		boolean pass = true;
		while (scanOut.hasNextLine() && scanAns.hasNextLine()){
			String outStr = scanOut.nextLine();
			String ansStr = scanAns.nextLine();
			if (!outStr.equals(ansStr)){
				pass = false;
				errorMessage = errorMessage + "Unequal outputs in file:OUT:"+outStr+";ANS:"+ansStr+"\n";
			}
		}
		
		if (scanOut.hasNextLine()){
			pass = false;
			errorMessage = errorMessage + "Extra rows outputted in file\n";
			fail();
		}
		
		if (scanAns.hasNextLine()){
			pass = false;
			errorMessage = errorMessage + "Not enough rows outputted in file\n";
			fail("Not enough rows outputted in file");
		}
		
		scanOut.close();
		scanAns.close();

		if (!pass){
			fail(errorMessage);	
		}
		
	}

}
