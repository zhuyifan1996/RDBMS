package db;

import java.io.File;
import java.io.IOException;

public interface TupleWriter {

	/**
	 * Set the output schema of the writer. 
	 * Once this is set, it will delete the file
	 * @param scma
	 */
	public void setOutputSchema(Schema scma);
	
	/**
	 * Set the output file source; will reset the file
	 * (clean it up) and start writing from the beginning. 
	 * Also set the schema of the file, which will restrict
	 * the tuple to be written.
	 * 
	 * @param file
	 * @param scma
	 * @return boolean whether success
	 */
	public boolean setOutputFile(File file);
	
	/**
	 * @return the file currently writing to
	 */
	public File getCurrentFile();
	
	/**
	 * @return the schema of the current tuple
	 */
	public Schema getCurrentSchema();
	
	/**
	 * Reset the writer, start to write it 
	 * from the beginning (clean it up first)
	 */
	public void reset();

	/**
	 * Write a tuple to the tuple writer
	 * @param tp [precondition] [tp] must conform to the current 
	 * 							schema to be written
	 * 							if there is no schema, then the
	 * 							set the schema to be the first seen ones
	 * @param tp
	 * @throws IllegalArgumentException
	 * 		if the tuple doesn't conform to the schema
	 * @throws IOException
	 */
	public void writeTuple(Tuple tp) throws IllegalArgumentException, IOException;
	
	/**
	 * Will close the current Writer
	 * need to reset everything to use it again.
	 * @throws IOException 
	 */
	public void close() throws IOException;
	
	/**
	 * Flush the writer to write everything on file
	 * @throws IOException 
	 */
	public void flush() throws IOException;
}
