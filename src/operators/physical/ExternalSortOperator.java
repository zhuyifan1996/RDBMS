package operators.physical;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import main.Logger;
import db.BufferTupleReader;
import db.BufferTupleWriter;
import db.CustomColumn;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleReader;
import db.TupleWriter;

public class ExternalSortOperator extends SortOperator {

	/**
	 * Buffer: each buffer is a fixed length array of Tuple Array
	 */
	private final int availableBuffers;
	private final int maxNumTpInBufferSlot; // dummy value

	/**
	 * The temporary directory of the file
	 */
	private String tmpDir;

	/**
	 * The tuple reader that can return the tuples in sorted order
	 */
	private TupleReader sortedReader = null;

	/**
	 * 
	 * @param child
	 * @param orderBy
	 * @param bufferSize
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public ExternalSortOperator(PhysicalOperator child, CustomColumn[] orderBy, int b){
		super(child, orderBy);
		this.availableBuffers = b;
		this.maxNumTpInBufferSlot = (SystemCatalogue.PAGE_SIZE - 4*2)/this.getSchema().getNumberOfColumns()/4;
	}

	/**
	 * Reset the operator,
	 * so that the next call of getNextTuple will return the tuple at [index]
	 * in the result relation.
	 */
	@Override
	public void reset(int index){
		if (this.sortedReader != null){
			this.sortedReader.reset(index);
		}else{
			try {
				this.sortedReader = sort();
				this.sortedReader.reset(index);
			} catch (IllegalArgumentException | IOException e) {
				Logger.warnln(e.getMessage());
			}
		}
	}

	@Override
	public Tuple getNextTuple(){
		if (this.sortedReader == null){
			try {
				this.sortedReader = sort();
				if (this.sortedReader== null){
					return null;
				}
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
				return null;
			}
		}		
		return this.sortedReader.getNextTuple();
	}

	/**
	 * This will perform the external sort
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private TupleReader sort() throws IllegalArgumentException, IOException{
		// create the subdirectory
		String tmpDirName = ((Integer) this.hashCode()).toString();
		this.tmpDir = SystemCatalogue.getTempPath() + tmpDirName + "/";
		File dir = new File(this.tmpDir);
		if (!dir.exists()){
			boolean ret = dir.mkdirs();
			assert(ret);
		}

		ArrayList<File> run0 = passZero();
		Logger.println("RUN0:"+run0);
		if (run0.size() == 1){
			return this.createTupleReader(run0.get(0));
		}else if (run0.size() == 0){
			File ret = new File(this.tmpDir+"ret");
			if (!ret.exists()) ret.createNewFile();
			return this.createTupleReader(ret);
		}
		
		ArrayList<File> readers =new ArrayList<File>(mergePass(run0, 0));

		if (readers.size() == 1){
			return this.createTupleReader(readers.get(0));
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
		return new BufferTupleReader(f, this.getSchema(), SystemCatalogue.PAGE_SIZE);
	}

	/**
	 * Factory Method that create the tuple writer for this operator
	 * @param f
	 * @return
	 */
	private TupleWriter createTupleWriter(File f){
		return new BufferTupleWriter(f, this.getSchema(), SystemCatalogue.PAGE_SIZE);
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

		Tuple nextTuple = this.child.getNextTuple();
		int currBufferSize = 0;
		int count = 0;
		outer:while(nextTuple != null){			
			// fill as much of the bufers as possible,
			this.clearBuffer(buffer);
			for (int i = 0; i < buffer.length; i++){				
				for (int j = 0; j < this.maxNumTpInBufferSlot; j++){
					buffer[i][j] = nextTuple;
					nextTuple = this.child.getNextTuple();
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
			Iterator<TupleReader> iter = inBufferRuns.iterator();
			while(iter.hasNext()){
				iter.next().getCurrentFile().delete();
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
		return "External("+this.availableBuffers+")"+super.toString();
	}

}
