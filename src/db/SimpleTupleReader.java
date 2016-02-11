package db;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Simple Tuple Reader use Scanner to implement TupleReader
 * @author Guandao Yang
 * 
 */
public class SimpleTupleReader implements TupleReader {

	private Scanner sc;
	private File inFile;
	private Schema schema;

	public SimpleTupleReader(File inFile, Schema schema) {
		this.schema = schema;
		this.setInputFile(inFile);
	}

	@Override
	public boolean setInputFile(File inDir) {
		this.inFile = inDir;
		try {
			this.sc = new Scanner(inFile);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}		
	}

	@Override
	public File getCurrentFile() {
		return this.inFile;
	}

	@Override
	public void reset() {
		this.sc.reset();
		System.err.println("DID RESET TUPLE READER!");
	}

	/**
	 * Takes a line from some input stream and outputs a Tuple object.
	 *  
	 * @param csvLine 	1. The csvLine must contains the exact same number of 
	 * 					elements as the number of columns in this schema.
	 * 				 	2. The csvLine elements must also be put into the same
	 * 					order as the corresponding columns of this schema
	 * @param delimiter Separator of the elements
	 * @return			The tuple that conforms to this schema and contains the
	 * 					data specified from the cvsLine
	 */
	public Tuple parse(String csvLine, String delimiter){
		String[] tmp = csvLine.trim().split(delimiter);
		Object[] data = new Object[tmp.length];

		for (int i = 0; i < tmp.length; i++){
			data[i] =  new Integer(tmp[i]);
		}

		if (this.schema == null){
			this.schema = SchemaWithDictionary.createDummySchemaForLength(data.length);
		}
		
		return new Tuple(this.schema, data);
	}

	@Override
	public Tuple getNextTuple() {
		if( sc.hasNextLine()){
			String data = sc.nextLine();
			Tuple t = this.parse(data, ",");
			return t;
		} else{
			return null;
		}
	}

	@Override
	public boolean hasNextTuple() {
		return sc.hasNextLine();
	}

	@Override
	public void close() {
		sc.close();
	}

	@Override
	public void reset(int index) {
		this.reset();	
		for (int i = 0; i < index-1; i++){
			this.getNextTuple();
		}
	}

	@Override
	public void reset(int pageIndex, int tupleIndex) {
		// TODO Auto-generated method stub
		
	}

}
