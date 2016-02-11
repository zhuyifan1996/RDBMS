package db;

import java.io.File;

/**
 * The class record id represents the rid in the system. 
 * It is a tuple of (File, pageIndex, tupleIndex)
 * @author Guandao Yang
 *
 */
public class RecordId {
	
	public final String fileName;
	public final int pageIndex;
	public final int tupleIndex;
	
	/**
	 * Initialize a record id representation from the binary file [this is required!]
	 * @param fileName
	 * @param pageIndex
	 * @param tupleIndex
	 */
	public RecordId(String binaryFileName, int pageIndex, int tupleIndex) {
		this.fileName = binaryFileName;
		this.pageIndex = pageIndex;
		this.tupleIndex = tupleIndex;
	}
	
	/**
	 * This method will use a buffer to read the tuple from the file everytime
	 * so it costs at least 1 IO every time. If you are using clusttered index,
	 * you shouldn't use it.
	 * @return the Tuple this record referrs to; null if such the record is invalid
	 */
	public Tuple getTuple(){
		BufferTupleReader reader = new BufferTupleReader(new File(this.fileName), SystemCatalogue.PAGE_SIZE);
		reader.reset(pageIndex, tupleIndex);
		return reader.getNextTuple();
	}

}
