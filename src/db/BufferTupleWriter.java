package db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A java nio tuple writer
 * @author Trevor Edwards, Guandao Yang
 *
 */
public class BufferTupleWriter implements TupleWriter {

	// Meta data
	private File outFile;									// output file for the writer
	private int pageSize = SystemCatalogue.PAGE_SIZE;		// pagesize for the tuple writer
	private Schema scma;									// output schemas
	private int tupleSize; 									// tuple size in ints	

	// JavaNIO stuffs
	private FileChannel channel;
	private ByteBuffer buffer;
	private int currBufferSize;			// how many tuples are stored inside current buffers
	private int[] tuples;				// [tuples] represents an arary that has only tuples data
	// it has the length of max number of tuples that can be put
	// into the buffer at the given setting

	public BufferTupleWriter(File outFile, Schema scma, int pageSize){
		this.outFile = outFile;
		this.pageSize = pageSize;
		if (scma != null){
			this.setOutputSchema(scma);	
		}
		
		this.setOutputFile(outFile);
		
	}

	@Override
	/**
	 * [precondition] have set pageSize
	 */
	public void setOutputSchema(Schema scma) {
		this.scma = scma;

		// calculate the tuple Size
		if (this.scma != null){
			this.tupleSize = this.scma.getNumberOfColumns();
			this.setUpBuffer();		
		}else{
			throw new IllegalArgumentException("Invalid schema");
		}
		
	}

	/**
	 * convert tuple into an int array
	 * @param tp
	 * @return
	 */
	private int[] getIntFromTuple(Tuple tp){
		Object[] dataArray = tp.getData();
		int[] ret = new int[dataArray.length];
		for (int i = 0; i < dataArray.length; i++){
			ret[i] = ((Integer)dataArray[i]);
		}
		return ret;
	}

	@Override
	public boolean setOutputFile(File file) {
		try {
			this.outFile = file;
			this.channel = (new FileOutputStream(this.outFile)).getChannel();
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// TODO
			return false;
		}
	}

	private void setUpBuffer(){
		if (this.buffer == null){
			this.buffer = ByteBuffer.allocate( this.pageSize );			
		}
		
		//this.buffer.clear();
		this.currBufferSize = 0;
		this.tuples = new int[(this.pageSize - 2 * 4)/4]; // number of integers for tuples that we can hold
	}
	

	@Override
	public File getCurrentFile() {
		return this.outFile;
	}

	@Override
	public Schema getCurrentSchema() {
		return this.scma;
	}

	@Override
	public void reset() {
		this.setOutputFile(outFile);		
	}

	@Override
	public void writeTuple(Tuple tp) throws IllegalArgumentException, IOException {
		if (this.scma == null){
			this.setOutputSchema(tp.getSchema());			
		}
		
		int[] data = getIntFromTuple(tp);
		// if the buffer isn't enough, then flush it and clear the buffer
		if (this.currBufferSize + data.length/this.tupleSize > this.tuples.length/this.tupleSize){
			this.flush();
			this.setUpBuffer();	
		}

		// now we should be ready to just write some tuples inside
		for (int i = 0 ; i < tp.numOfAttributes(); i++){			
			this.tuples[this.currBufferSize * this.tupleSize + i] = data[i];
		}
		
		this.currBufferSize++;


	}

	@Override
	/**
	 * Flush the current buffer, close the file channel.
	 */
	public void close() throws IOException {
		this.flush();
		this.channel.close();
	}

	@Override
	public void flush() throws IOException {
		if (this.currBufferSize != 0){
			this.buffer.clear();
			this.buffer.putInt(this.tupleSize);
			this.buffer.putInt(this.currBufferSize);
			
			int c = 0;
			int max = this.pageSize/4 - 2; // total number of int that can be put inside the
			for (int i = 0; i < this.currBufferSize; i++){
				for (int j = 0 ; j < this.tupleSize; j++){
					this.buffer.putInt(this.tuples[i*this.tupleSize + j]);
					c++;
				}
			}
			
			while(c < max){
				this.buffer.putInt(0);
				c++;
			}

			this.buffer.flip();
			this.channel.write(this.buffer);	
		}

	}


}
