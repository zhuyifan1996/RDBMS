package operators.util;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;

import main.Logger;
import db.BufferTupleReader;
import db.BufferTupleWriter;
import db.CustomColumn;
import db.SimpleTupleReader;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleReader;
import db.TupleWriter;

public class ExternalMergeSort {
	// Temporary directory
	private final String tmpPath;
	private String tmpDir;
	public boolean clean = true;
	
	// input
	private TupleReader inputReader;
	private Comparator<Tuple> comparator;

	/**
	 * Buffer: each buffer is a fixed length array of Tuple Array
	 */
	private int availableBuffers = 20;
	private int maxNumTpInBufferSlot = -1; // dummy value

	// THe Output
	private File sortedFile = null;

	public ExternalMergeSort(File toBeSorted, boolean isBinaryFile, String tmpPath, Comparator<Tuple> comparator) {
		this.tmpPath = tmpPath;
		if (isBinaryFile){
			this.inputReader = new BufferTupleReader(toBeSorted, null, SystemCatalogue.PAGE_SIZE);
		}else{
			this.inputReader = new SimpleTupleReader(toBeSorted, null);
		}
		
		this.comparator = comparator;
	}

	/**
	 * Configurate the sort options
	 * @param maxNumTpInBufferSlot
	 * @param availableBuffers
	 */
	public void config(int maxNumTpInBufferSlot, int availableBuffers){
		this.maxNumTpInBufferSlot = maxNumTpInBufferSlot;
		this.availableBuffers = availableBuffers;
	}

	/**
	 * Return the sorted File
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */
	public File getSortedFile() throws IllegalArgumentException, IOException, NoSuchAlgorithmException{
		if (this.sortedFile == null){
			this.sortedFile = sort();
		}
		return this.sortedFile;
	}

	private String generateFileName() throws NoSuchAlgorithmException{
		return ((Integer) this.hashCode()).toString();
	}
	
	/**
	 * This will perform the external sort
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchAlgorithmException 
	 */
	private File sort() throws IllegalArgumentException, IOException, NoSuchAlgorithmException{
		// create the subdirectory
		String tmpDirName = generateFileName();
		this.tmpDir = this.tmpPath + tmpDirName + "/";
		File dir = new File(this.tmpDir);
		if (!dir.exists()){
			boolean ret = dir.mkdirs();
			assert(ret);
		}

		ArrayList<File> run0 = passZero();
		Logger.println("RUN0:"+run0);
		if (run0.size() == 1){
			return run0.get(0);
		}else if (run0.size() == 0){
			File ret = new File(tmpPath+"ret");
			if (!ret.exists()) ret.createNewFile();
			return ret;
		}

		ArrayList<File> readers =new ArrayList<File>(mergePass(run0, 0));

		if (readers.size() == 1){
			return readers.get(0);
		}else{
			System.err.println("Reader"+readers);
			return null;
		}

	}

	/**
	 * Factory Method that create the tuple reader for this operator
	 * @param f
	 * @return
	 */
	private TupleReader createTupleReader(File f){
		return new BufferTupleReader(f, null, SystemCatalogue.PAGE_SIZE);
	}

	/**
	 * Factory Method that create the tuple writer for this operator
	 * @param f
	 * @return
	 */
	private TupleWriter createTupleWriter(File f){
		return new BufferTupleWriter(f, null, SystemCatalogue.PAGE_SIZE);
	}

	/** 
	 * @return an array list of tuple reader, each of which will represents a run
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private ArrayList<File> passZero() throws IllegalArgumentException, IOException{
		// This will be runs
		ArrayList<File> ret = new ArrayList<File>();

		Tuple[][] buffer = new Tuple[this.availableBuffers-1][];
		
		Tuple nextTuple = this.inputReader.getNextTuple();
		int currBufferSize = 0;
		int count = 0;
		outer:while(nextTuple != null){	
			if (this.maxNumTpInBufferSlot == -1){
				this.maxNumTpInBufferSlot = (SystemCatalogue.PAGE_SIZE - 2*4)/(nextTuple.getSchema().getNumberOfColumns()*4);
			}
			
			// fill as much of the buffers as possible,
			this.clearBuffer(buffer);
			for (int i = 0; i < buffer.length; i++){				
				for (int j = 0; j < this.maxNumTpInBufferSlot; j++){
					buffer[i][j] = nextTuple;
					nextTuple = this.inputReader.getNextTuple();
					currBufferSize++;
					if (nextTuple == null) break outer;
				}
			}

			// then we just want to sort the tuples
			File outFile = new File(this.tmpDir+"run0_"+count++);
			TupleWriter writer = this.createTupleWriter(outFile);
			Logger.println("Run "+count+":"+currBufferSize);
			sortAndWriteBuffer(buffer, currBufferSize, writer);
			writer.close();
			ret.add(outFile);
			currBufferSize = 0;
		}

		if (currBufferSize != 0){
			File outFile = new File(this.tmpDir+"run0_"+count++);
			TupleWriter writer = this.createTupleWriter(outFile);

			Logger.println("Run "+count+":"+currBufferSize);

			sortAndWriteBuffer(buffer, currBufferSize, writer);
			writer.close();
			ret.add(outFile);			
		}		

		return ret;
	}

	/**
	 * Reset the buffer
	 */
	private void clearBuffer(Tuple[][] buffer){
		for (int i = 0; i < buffer.length; i++){
			buffer[i] = new Tuple[this.maxNumTpInBufferSlot];
		}	
	}

	/**
	 * Will put [currBufferSize] number of tuples out from the buffer and sort them ,write them back to the [writer]
	 * [precondition] the buffer is filled from [0,0] -> [x,y] and there is no empty seats in between
	 * @return
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private void sortAndWriteBuffer(Tuple[][] buffer, int currBufferSize, TupleWriter writer) throws IllegalArgumentException, IOException{
		ArrayList<Tuple> sorted = new ArrayList<Tuple>();
		int count = 0; 
		sortOuter:for (int i = 0; i < buffer.length; i++){
			for (int j = 0; j < this.maxNumTpInBufferSlot; j++){
				sorted.add(buffer[i][j]);
				if (buffer[i][j] ==  null) System.err.println("null in ("+i+","+j+")");
				count++;
				if (count == currBufferSize) break sortOuter;
			}
		}
		
		if (this.comparator==null){
			// make a full comparator based on the whole schema
			Tuple sampleTuple = buffer[0][0];
			final CustomColumn[] colArr = sampleTuple.getSchema().getColumns();			
			this.comparator = new Comparator<Tuple>(){
				@Override
				public int compare(Tuple t1, Tuple t2) {
					if (t1 != null) return t1.customCompare(colArr, t2);
					if (t2 != null) return t2.customCompare(colArr, t1);
					Logger.warnln("BOTH T1 and T2 are NULL: "+t1+","+t2);
					return 0;
				}
			};
		}
		
		sorted.sort(this.comparator);

		for (Tuple tp: sorted){
			writer.writeTuple(tp);
		}
	}
	
	/**
	 * This function will run the merge Pass until it converge into 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private Collection<File> mergePass(Collection<File> runsFiles, int numPass) throws IllegalArgumentException, IOException{
		Logger.println("Merge Pass " + numPass);

		ArrayList<File> ret = new ArrayList<File>();
		Stack<File> stk = new Stack<File>();
		stk.addAll(runsFiles);		

		int runCounter = 0;
		while(!stk.isEmpty()){
			int bufCounter = 0;
			ArrayList<TupleReader> inBufferRuns = new ArrayList<TupleReader>();			
			while(!stk.isEmpty() && bufCounter < this.availableBuffers - 1){	// -1 because we have one out buffer
				inBufferRuns.add(this.createTupleReader(stk.pop()));
				bufCounter++;
			}
			Tuple[] buffer = new Tuple[bufCounter];

			// then do the merge
			File runFile = new File(this.tmpDir+"pass_"+numPass+"_run_"+runCounter);
			runCounter++;
			TupleWriter writer = this.createTupleWriter(runFile);

			// merge from different buffers
			while(true){
				// find the minimal
				// as long as there is one more things to merge, we will continue				
				boolean flag = false;
				for (int i = 0 ; i < buffer.length; i++){
					if (buffer[i] == null) 
						buffer[i] = inBufferRuns.get(i).getNextTuple();
					flag = flag || (buffer[i] != null);
				}
				if (!flag) break; // nothing else to load, nothing else to do

				// find the minial tuple, write that into the writer, 
				// and put that buffer position to null
				int minIndex = 0;
				for (int i = 1; i < buffer.length; i++){
					if (buffer[minIndex] == null){
						minIndex = i;
					}else if (buffer[i] != null){
						minIndex = this.comparator.compare(buffer[minIndex], buffer[i]) >= 0 && buffer[i] != null ? i : minIndex;	
					}					
				}
				if (buffer[minIndex] == null) System.err.println("Got null in min buffer element:"+buffer[minIndex]);
				writer.writeTuple(buffer[minIndex]);
				buffer[minIndex] = null;
			}

			// finish the run, add the run reader into return result
			writer.close();
			ret.add(runFile);			

			// clean up
			if (clean){
				Iterator<TupleReader> iter = inBufferRuns.iterator();
				while(iter.hasNext()){
					iter.next().getCurrentFile().delete();
				}	
			}
			
		}

		Logger.println("Merge Pass " + numPass +" ends with: "+ret);

		// recurse if the total number of ret is larger than the
		if (ret.size() <= 1)
			return ret;
		else
			return this.mergePass(ret, numPass+1);		
	}

	@Override
	public String toString(){
		return "ExternalMergeSort";
	}
}
