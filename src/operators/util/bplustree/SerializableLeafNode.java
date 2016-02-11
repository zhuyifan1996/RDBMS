package operators.util.bplustree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import db.RecordId;
import db.SystemCatalogue;

public class SerializableLeafNode extends LeafNode<Integer, ArrayList<RecordId>> 
		implements SerializableNode<Integer, ArrayList<RecordId>> {
	
	private int serial_number;
	public int getSerialNumber(){return serial_number;}
	public void setSerialNumber(int n){serial_number = n;}
	
	/**
	 * Default constructor
	 * @param newKeys
	 * @param newValues
	 * @param order
	 */
	public SerializableLeafNode(List<Integer> newKeys, List<ArrayList<RecordId>> newValues, int order) {
		super(newKeys, newValues, order);
	}
	
	/**
	 * Copy constructor
	 * @param leaf
	 */
	public SerializableLeafNode(LeafNode<Integer, ArrayList<RecordId>> leaf){
		super(leaf.keys, leaf.values, leaf.getOrder());
	}
	
	public SerializableLeafNode(LeafNode<Integer, ArrayList<RecordId>> leaf, int s_number){
		this(leaf);
		serial_number=s_number;
	}

	@Override
	public void deSerialize(File in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deSerialize(FileChannel channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serialize(File out) {
		FileChannel channel;
		//setup channel and buffer
		try {
			channel = (new FileOutputStream(out)).getChannel();
		} catch (FileNotFoundException e) {
			System.err.println("Fail to serialize B+ tree leaf node: "+this);
			e.printStackTrace();
			return;
		}
		this.serialize(channel);
	}

	@Override
	public void serialize(FileChannel channel) {
		ByteBuffer buffer = null;
		buffer = ByteBuffer.allocate( SystemCatalogue.PAGE_SIZE );
		
		//Put 0 to indicate this is a leaf node
		buffer.putInt(0);
		//Put in the number of data entries in this leaf
		buffer.putInt(this.values.size());
		//Put in data entries in format of [key]+[number of rids]+[rids]
		for(int i=0; i<this.values.size(); i++){
			buffer.putInt(this.keys.get(i));
			buffer.putInt(this.values.get(i).size());
			for(RecordId rid:this.values.get(i)){
				//Put in rid = pageIndex + tupleIndex
				buffer.putInt(rid.pageIndex);
				buffer.putInt(rid.tupleIndex);
			}
		}
		
		//Write to file
		buffer.flip();
		try {
			channel.write(buffer);
			buffer.clear();
		} catch (IOException e) {
			System.err.println("Fail to serialize B+ tree leaf node: "+this);
			e.printStackTrace();
		}
	}

}
