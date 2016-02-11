package db;

import java.io.File;

/**
 * The interface the methods needed to implemented as 
 * a tuple reader from
 * @author Guandao Yang
 */
public interface TupleReader {
	
	/**
	 * Specified the input file source
	 * This function should also reset the tuple reader 
	 * @param inDir
	 * @return whether success
	 */
	public boolean setInputFile(File inDir);
	
	/**
	 * @return the file currently being read
	 */
	public File getCurrentFile();
	
	/**
	 * Reset the reader and put it back to the first tuple 
	 * of the input file
	 */
	public void reset();
	
	/**
	 * Reset the reader to start returning from the [index]th 
	 * indexed tuple of the tuple reader
	 * @param index 	the index of the tuple (from zero)
	 */
	public void reset(int index);
	
	/**
	 * Reset the tuple reader to a certain position 
	 * according to rid = <file, pageIndex, tupleIndex>
	 * @param pageIndex
	 * @param tupleIndex
	 */
	public void reset(int pageIndex, int tupleIndex);

	/**
	 * @return the next tuple from the current directory
	 */
	public Tuple getNextTuple();
	
	/**
	 * @return whether we have the next tuple in the current directory
	 */
	public boolean hasNextTuple();
	
	/**
	 * Will close the current Writer
	 * need to reset everything to use it again.
	 */
	public void close();
		
}
