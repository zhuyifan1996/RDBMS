package operators.util.bplustree;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import db.BufferTupleReader;
import db.CustomColumn;
import db.RecordId;
import db.Schema;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleReader;
import main.Logger;
import main.Main;
import operators.util.ExternalMergeSort;

public class BPlusTreeBulkLoader{

	private File file;	
	private File sorted;
	
	private CustomColumn col;
	private Schema scma;
	private int order;
	private boolean clustered;

	private int prevLevelCount = 0;	// how many nodes have we collected at the previous level
	private int level = 0;			// how many levels have we collected
	
	private SerializableBPlusTree ret = null;
	
	public BPlusTreeBulkLoader(File file, CustomColumn col, Schema scma, int order, boolean clustered){
		this.file = file;
		this.col = col;
		this.scma = scma;
		this.order = order;
		this.clustered = clustered;
	}

	/**
	 * Bulk Loading from [reader] and build a B+ tree under order [order]
	 * If [clustered] is set to true, then we will create a serialized
	 * file and and save the sorted file to the system directory 
	 * for where the index lives
	 * 
	 * [precondition] The whole B+ tree is assumed to be fit in memroy
	 * 
	 * @param file
	 * @param comp
	 * @param scma
	 * @param order
	 * @param clustered
	 * @return 	A SerializableBPlusTree that come from the Bulk Loader
	 * 			return null if failed
	 * 
	 */
	public SerializableBPlusTree bulkLoad() {
		// If we have already stored the result, return it directly
		if (this.ret != null){
			return this.ret;
		}
		
		//Sort the file
		Comparator<Tuple> comp = getComparator(col);
		ExternalMergeSort sorter = new ExternalMergeSort(file, true, Main.DEFAULT_TEMP_DIRECTORY, comp);
		try {
			this.sorted = sorter.getSortedFile();
			if (this.sorted == null){
				throw new IllegalArgumentException("Failed to sort the file");
			}
		} catch (IllegalArgumentException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			Logger.warnln(e.getMessage());
			return null;
		}

		if (clustered){
			//replace old file with new one
			
		}

		// start bulk loading
		LeafNode<Integer,ArrayList<RecordId>> headLeaf = buildLeaves();
		if (headLeaf.nextLeaf == null){
			// this tree has only one level
			this.ret = new SerializableBPlusTree(new SerializableLeafNode(headLeaf), this.order);
			return this.ret;
		}
		
		
		
		return null;
	}
	
	/**
	 * 
	 * @author Guandao Yang
	 *
	 */
	private class MinKeyEntry implements Entry<Integer, Node<Integer, ArrayList<RecordId>>> {

		private Integer minKey;
		private Node<Integer, ArrayList<RecordId>> node;
		
		public MinKeyEntry(Integer minKey, Node<Integer, ArrayList<RecordId>> node){
			this.minKey = minKey;
			this.node = node;
		}
		
		@Override
		public Integer getKey() {
			return this.minKey;
		}

		@Override
		public Node<Integer, ArrayList<RecordId>> getValue() {
			return this.node;
		}

		@Override
		public Node<Integer, ArrayList<RecordId>> setValue(Node<Integer, ArrayList<RecordId>> value) {
			this.node = value;
			return this.node;
		}
		
	}
	
	/**
	 * 
	 * @author Guandao Yang
	 */
	private class LeafMinKeyIter implements Iterator<Entry<Integer, Node<Integer, ArrayList<RecordId>>>>{

		private LeafNode<Integer, ArrayList<RecordId>> currNode;
		
		public LeafMinKeyIter(LeafNode<Integer, ArrayList<RecordId>> head){
			this.currNode = head;
		}
		
		@Override
		public boolean hasNext() {
			return currNode.nextLeaf != null;
		}

		@Override
		public Entry<Integer, Node<Integer, ArrayList<RecordId>>> next() {
			LeafNode<Integer, ArrayList<RecordId>> ret = this.currNode.nextLeaf;
			Integer minKey = ret.keys.get(0);
			
			// update the state
			this.currNode = this.currNode.nextLeaf;
			return new MinKeyEntry(minKey, ret);
		}		
	}

	/**
	 * [precondition] iter has more than one number
	 * @param iter
	 * @return
	 */
	private Node<Integer,ArrayList<RecordId>> buildIndices(Iterable<Entry<Integer, Node<Integer, ArrayList<RecordId>>>> iter){
		ArrayList<Entry<Integer, Node<Integer, ArrayList<RecordId>>>> ret = 
				new ArrayList<Entry<Integer, Node<Integer, ArrayList<RecordId>>>>();
		
		int count = 0;
		
		IndexNode<Integer, ArrayList<RecordId>> currIndex = new IndexNode<Integer, ArrayList<RecordId>>(
				new ArrayList<Integer>(),
				new ArrayList<Node<Integer,ArrayList<RecordId>>>(), 
				this.order);
		
		// this array stores the keys starting from the minimal keys
		// from [this.order + 1]th node to that of its last one
		// (since one of these minKeys will be needed for redistribution)
		ArrayList<Integer> lastMinKeys = new ArrayList<Integer>();
		int secToTheLastCount = this.prevLevelCount  - (this.prevLevelCount % (2*this.order)) - this.order ;
		
		for(Entry<Integer, Node<Integer, ArrayList<RecordId>>> entry : iter){			
			count++;			
			
			// if it is zero, then put the first node into the value
			if (currIndex.children.size() == 0){				
				currIndex.children.add(entry.getValue());
				Entry<Integer, Node<Integer, ArrayList<RecordId>>> newEntry =
						new MinKeyEntry(entry.getKey(), currIndex);
				ret.add(newEntry);
				continue;
			}
			
			// if it is overflow, then update the current Index
			if (currIndex.children.size() == 2*this.order){
				currIndex = new IndexNode<Integer, ArrayList<RecordId>>(
						new ArrayList<Integer>(),
						new ArrayList<Node<Integer,ArrayList<RecordId>>>(), 
						this.order);
				
				currIndex.children.add(entry.getValue());
				Entry<Integer, Node<Integer, ArrayList<RecordId>>> newEntry =
						new MinKeyEntry(entry.getKey(), currIndex);
				
				ret.add(newEntry);
				
				continue;
			}
			
			// other wise, the normal case
			currIndex.keys.add(entry.getKey());
			currIndex.children.add(entry.getValue());
			
			// record the keys from the last to the second nodes
			if (count >= secToTheLastCount){
				lastMinKeys.add(entry.getKey());
			}
		}
		
		// redistribute the last two if needed
		if (ret.size() >= 2){
			Entry<Integer, Node<Integer, ArrayList<RecordId>>> last = ret.get(ret.size()-1);
			Entry<Integer, Node<Integer, ArrayList<RecordId>>> sLast= ret.get(ret.size()-2);
			
			IndexNode<Integer, ArrayList<RecordId>> lastIndex = 
					(IndexNode<Integer, ArrayList<RecordId>>) last.getValue();
			IndexNode<Integer, ArrayList<RecordId>> sLastIndex= 
					(IndexNode<Integer, ArrayList<RecordId>>) sLast.getValue();
			
			// when we need to redistribute
			if (lastIndex.children.size() <  this.order){
				int numOfRemainingKeysInSLast = sLastIndex.keys.size() + lastIndex.keys.size() - this.order;
				for (int i = this.order * 2 - 1; i >= numOfRemainingKeysInSLast; i--){
					lastIndex.keys.add(0, sLastIndex.keys.get(i));
					sLastIndex.keys.remove(i);
					
					lastIndex.children.add(0, sLastIndex.children.get(i+1));
					sLastIndex.children.remove(i+1);
				}
				
				// update the second to the last index
				sLast.setValue(sLastIndex); 
				
				// update the last index
				// the key for the last will be the lowest key for sLastIndex.children.get(numOfRemainingKeysInSLast)
				// which is the [numOfRemainingKeysInSLast - this.order] indexed entry in the array lastMinKeys
				Integer k = lastMinKeys.get(numOfRemainingKeysInSLast - this.order);
				last = new MinKeyEntry(k, lastIndex);
				ret.set(ret.size(), last);	
			}			
		}
		
		// pompt up the level
		this.level++;
		this.prevLevelCount = count;
		if (ret.size() == 1){
			return ret.get(0).getValue();
		}else{
			return buildIndices(ret); 
		}
	}
	
	/**
	 * Build a doubly linked list by traversing the sorted file
	 * @return A doubly linked list of all the leaf nodes
	 */
	private LeafNode<Integer,ArrayList<RecordId>> buildLeaves(){
		// pass one, scan the whole tuple
		TupleReader reader = new BufferTupleReader(this.sorted, this.scma, SystemCatalogue.PAGE_SIZE);
		int maxNumTuplePerPage = (SystemCatalogue.PAGE_SIZE - 4*2)/(scma.getNumberOfColumns()*4);

		int totalTuple = 0;										// how many tuples have we collected
		int keyCount = 0;										// how many k-v pairs have we collected for the node
		Integer currKey = null;									// the key we are currently collecting
		ArrayList<RecordId> rids = new ArrayList<RecordId>();	// the current rids

		// A linked list that keep track of all the leaves
		LeafNode<Integer,ArrayList<RecordId>> headLeaf = null;
		LeafNode<Integer,ArrayList<RecordId>> currLeaf = null;

		while(reader.hasNextTuple()){
			Tuple tp = reader.getNextTuple();			
			Integer valueAtKeyPosition = ((Integer)tp.getDataForColumn(col));
			totalTuple++;

			// initialization
			if (currKey == null){
				currKey = valueAtKeyPosition;
				keyCount = 1;
				prevLevelCount = 1;
				currLeaf = new SerializableLeafNode(
						new ArrayList<Integer>(), new ArrayList<ArrayList<RecordId>>(), this.order );
				headLeaf = currLeaf;
				currLeaf.keys.add(currKey);
				currLeaf.values.add(rids);
				assert(currKey != null);
			}

			if (valueAtKeyPosition.compareTo(currKey) != 0) { // have changed key
				// change key, change rids array				
				currKey = valueAtKeyPosition;
				rids = new ArrayList<RecordId>();				

				// the node doesn't fit now
				keyCount++;				
				if (keyCount > 2 * this.order){
					keyCount = 1;
					LeafNode<Integer,ArrayList<RecordId>> tempLeaf = new SerializableLeafNode(
							new ArrayList<Integer>(), new ArrayList<ArrayList<RecordId>>(), this.order );

					// linked with the previous node
					currLeaf.nextLeaf = tempLeaf;
					tempLeaf.previousLeaf = currLeaf;

					// update the current pointer
					currLeaf = tempLeaf;

					// update the counter
					prevLevelCount++;		
				}

				// now that we got all the current key and rids
				currLeaf.keys.add(currKey);
				currLeaf.values.add(rids);
			}

			// now we have correct keyCount and leafCount, 
			// also have the correct rids
			int pageNum = (totalTuple - 1)/maxNumTuplePerPage;
			int tupleNum = (totalTuple - 1) % maxNumTuplePerPage;
			RecordId rid = new RecordId(sorted.getPath(), pageNum, tupleNum);
			rids.add(rid);
		}
		
		// redistribute when
		// 1. this isn't the last level (i.e. >1 leafes)
		// 2. the last leaf is underflow (i.e. key/value.size() < order)
		LeafNode<Integer, ArrayList<RecordId>> prev = currLeaf.previousLeaf;
		while(currLeaf.values.size() < this.order && currLeaf.keys.size() < this.order && prev != null){			
			currLeaf.keys.add(0, prev.keys.get(prev.keys.size()));
			currLeaf.values.add(0, prev.values.get(prev.values.size()));
		}
		
		this.level++;
		return headLeaf;
	}
	
	/**
	 * Get the tuple comparator for a single column of the index
	 * @param column
	 * @return Comparator
	 */
	private static Comparator<Tuple> getComparator(CustomColumn column){
		final CustomColumn col = column.clone();
		return (new Comparator<Tuple>(){
			@Override
			public int compare(Tuple o1, Tuple o2) {
				return o1.columnwiseCompare(col, col, o2);
			}			
		});	
	}

}
