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

public class SerializableIndexNode extends IndexNode<Integer, ArrayList<RecordId>> 
		implements SerializableNode<Integer, ArrayList<RecordId>>  {
	
	/**Override children field to account for serializable node*/
	protected ArrayList<SerializableNode<Integer, ArrayList<RecordId>>> children; 
	
	private int serial_number;
	public int getSerialNumber(){return serial_number;}
	public void setSerialNumber(int n){serial_number = n;}
	
	/**
	 * Default constructor 
	 * Modified to account for serializable node.
	 * @param newKeys
	 * @param newChildren
	 * @param order
	 */
	public SerializableIndexNode(List<Integer> newKeys, 
								List<SerializableNode<Integer, ArrayList<RecordId>> > newChildren,
								int order,
								int s_number) {
		super(order);
		isLeafNode = false;
		keys = new ArrayList<Integer>(newKeys);
		children = new ArrayList<SerializableNode<Integer, ArrayList<RecordId>>>(newChildren);
		serial_number = s_number;
	}
	
	/**
	 * Copy constructor
	 * @param index
	 */
	public SerializableIndexNode(IndexNode<Integer, ArrayList<RecordId>> index){
		super(index.keys, index.children, index.getOrder());
	}
	
	public SerializableIndexNode(IndexNode<Integer, ArrayList<RecordId>> index, int s_number){
		super(index.keys, index.children, index.getOrder());
		this.serial_number = s_number;
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
			System.err.println("Fail to serialize B+ tree index node: "+this);
			e.printStackTrace();
			return;
		}
		this.serialize(channel);
	}

	@Override
	public void serialize(FileChannel channel) {
		ByteBuffer buffer = null;
		buffer = ByteBuffer.allocate( SystemCatalogue.PAGE_SIZE );
		//Int 1 to indicate this is a index node
		buffer.putInt(1);
		//Put in number of keys 
		buffer.putInt(this.children.size()-1);
		//Put in all key keys in order
		for (Integer i:this.keys){
			buffer.putInt(i);
		}
		//Put in the serial number of the children
		for (SerializableNode<Integer, ArrayList<RecordId>> n:this.children){
			buffer.putInt(n.getSerialNumber());
		}
		//Write to file
		buffer.flip();
		try {
			channel.write(buffer);
			buffer.clear();
		} catch (IOException e) {
			System.err.println("Fail to serialize B+ tree index node: "+this);
			e.printStackTrace();
		}
	}

}
