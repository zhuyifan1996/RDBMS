package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import main.Logger;

/**
 * Buffer Tuple Reader acts as a buffer in the DBMS
 * @author Guandao Yang
 *
 */
public class BufferTupleReader implements TupleReader {

	private File inFile;
	private FileInputStream in;
	private FileChannel fileChannel;
	private ByteBuffer buffer;

	private int[] ipArr;
	private int tupleSize;
	private int tupleNumber;

	/**
	 * 0 <= [currTupleOffset] <= [tupleSize] - 1
	 * The next tuple to be output will be:
	 * 	ipArr[currTupleOffset+2] <--> ipArr[currTupleOffset+2+tupleSize] 
	 */
	private int nextTupleOffset;

	/**
	 * The size of the page in bytes. 
	 * Each buffer will have the size of the page.
	 */
	public final int pageSize;	

	private Schema scma;

	/**
	 * Construct a BufferTupleReader using [bufferSize]
	 * @param pageSize	#of bytes in the buffered
	 */
	public BufferTupleReader(File inFile, Schema scma, int pageSize){
		this.pageSize  = pageSize; 
		this.scma = scma;
		if (this.scma == null){
			// Logger.warnln("Reader contains no schema. Schemaless reading.");
		}
		setInputFile(inFile);
	}

	public BufferTupleReader(File inFile, int pageSize){
		this.pageSize = pageSize;
		setInputFile(inFile);
	}

	@Override
	public boolean setInputFile(File inDir) {
		try {
			this.inFile 	 = inDir;
			this.in 		 = new FileInputStream(this.inFile);
			this.fileChannel = in.getChannel();
			this.ipArr = null;
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
		this.close();
		this.setInputFile(this.inFile);
	}

	@Override
	public Tuple getNextTuple() {
		if (this.hasNextTuple()){
			// so we do get the ipArr
			Integer[] data = new Integer[this.tupleSize];
			for (int i = 0; i < this.tupleSize; i++){				
				data[i] = this.ipArr[2+this.nextTupleOffset*this.tupleSize + i];
			}			
			this.nextTupleOffset ++;

			// create a dummy schema if the schema doesn't exist
			if (this.scma == null){
				//Logger.warnln("Creating a dummy");
				this.scma = SchemaWithDictionary.createDummySchemaForLength(this.tupleSize);
			}

			return new Tuple(this.scma, data);			
		}
		return null;
	}

	@Override
	/**
	 * ASSUMPTION: all the tuples are consecutively aligned (no empty buffer in the middle)
	 * @return
	 */
	public boolean hasNextTuple() {
		try {
			// two cases when we cannot read the next tuple without
			// reading a new chunck of data:
			//		1. we just initialized the channel, nothing in the buffer yet
			//		2. the nextTuple is outside of the buffer
			if (this.ipArr == null || this.nextTupleOffset >= this.tupleNumber){				
				this.ipArr = new int[this.pageSize/4];
				this.buffer = ByteBuffer.allocateDirect(pageSize);
				int len = 0;

				if ((len = this.fileChannel.read(this.buffer)) == -1){					
					return false; // no next tuple
				}				
				this.buffer.flip();												
				for (int i = 0; i < len/4; i++){
					this.ipArr[i] = this.buffer.getInt();
				}
				if (this.ipArr[0] > this.pageSize) Logger.warnln("Redicular tuple size:"+this.ipArr[0]);
				
				this.tupleSize 			= this.ipArr[0];
				this.tupleNumber 		= this.ipArr[1];
				this.nextTupleOffset 	= 0;

				return this.tupleNumber!=0;
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void close() {
		try {
			this.fileChannel.close();
			this.buffer.clear();
			this.tupleNumber = 0;
			this.tupleSize = 0;
			this.nextTupleOffset = 0;
		} catch (IOException e) {
			Logger.warnln(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void reset(int index) {
		// calculate the page
		int maxNumberTuple = (this.pageSize - 2*4)/this.tupleSize;
		int pageIndex = index/maxNumberTuple;
		int tupleIndex = index % maxNumberTuple;
		System.err.println("("+maxNumberTuple+","+pageIndex+","+tupleIndex+")");
		try {
			// need to reset the file channel back to the original
			this.fileChannel.position(pageIndex*this.pageSize);

			this.ipArr = new int[this.pageSize/4];
			this.buffer = ByteBuffer.allocateDirect(pageSize);
			int len = 0;

			if ((len = this.fileChannel.read(this.buffer)) == -1){					
				throw new IllegalArgumentException("Invalid indexes");
			}				
			this.buffer.flip();												
			for (int i = 0; i < len/4; i++){
				this.ipArr[i] = this.buffer.getInt();
			}

			this.tupleSize 			= this.ipArr[0];
			this.tupleNumber 		= this.ipArr[1];

			// set it to the offset we want
			this.nextTupleOffset = tupleIndex;
		} catch (IOException e) {
			Logger.warnln(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void reset(int pageIndex, int tupleIndex) {
		// TODO Auto-generated method stub
		
	}

}
