package db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import main.Logger;

/**
 * Converts data from human readable to bytecode and back
 * @author Trevor Edwards
 *
 */
public class NIODataConverter {
		
	/**
	 * A main for easy on the fly conversion
	 */
	public static void main(String[] args) throws IllegalArgumentException, IOException{
		
		String OUTPUT_PATH = "";
		String TO_CONVERT_FOLDER_PATH = "";
		boolean isByteToHum = true;
		if (args.length < 3){

			OUTPUT_PATH = "test/graded/expected_sorted_convert/";
			TO_CONVERT_FOLDER_PATH = "test/graded/expected_sorted/";	 
		}else{
			OUTPUT_PATH = args[0];
			TO_CONVERT_FOLDER_PATH = args[1];
			isByteToHum = args[2].equals("b");
		}
		
		//convert human to bytes
		File inFolder = new File(TO_CONVERT_FOLDER_PATH);
		File outPath = new File(OUTPUT_PATH);
		if (!outPath.exists()){
			outPath.mkdirs();
		}

		ArrayList<String> exclude = new ArrayList<String>(Arrays.asList(new String[]{"README"}));
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(inFolder.list()));
		names.removeAll(exclude);
		for (String name : names){
			if (name.indexOf(".")==0){
				continue;
			}
			
			Logger.println("Converting file:"+name);
			if (isByteToHum){
				if (name.contains("_humanreadable") || name.contains(".DS_Store")) continue;
				byteToHuman(TO_CONVERT_FOLDER_PATH + name, OUTPUT_PATH+name+"_humanreadable");	
			}else{
				humanToByte(TO_CONVERT_FOLDER_PATH + name, OUTPUT_PATH+name);
			}
		}
	}
	
	public static void byteToHuman(String inFile, String outFileName) throws IllegalArgumentException, IOException{
		File outFile = new File(outFileName);		
		TupleReader btr = new BufferTupleReader(new File(inFile), SystemCatalogue.PAGE_SIZE);
		TupleWriter stw = new SimpleTupleWriter(outFile);
		while( true ){
			Tuple t = btr.getNextTuple();
			if( t != null)
				stw.writeTuple( t );
			else
				break;
		}
		btr.close();
		stw.close();
	}
	
	public static void humanToByte(String inFile, String outFileName) throws IllegalArgumentException, IOException{
		//get a scheme yo
		File outFile = new File(outFileName);
		TupleReader str = new SimpleTupleReader(new File( inFile), null);
		TupleWriter btw = new BufferTupleWriter(outFile, null, SystemCatalogue.PAGE_SIZE);
		
		while( true ){
			Tuple t = str.getNextTuple();
			if (btw.getCurrentSchema() == null){
				btw.setOutputSchema(t.getSchema());
			}
			
			if( t != null)
				btw.writeTuple( t );
			else
				break;
		}
		btw.close();
	}
	

}
