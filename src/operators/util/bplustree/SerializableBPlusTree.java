package operators.util.bplustree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import db.RecordId;
import db.SystemCatalogue;

public class SerializableBPlusTree extends BPlusTree<Integer, ArrayList<RecordId>> implements Serializable {

	SerializableNode<Integer, ArrayList<RecordId>> root;
	/**Stores the order in which the nodes will be serialized. Leaf first from left
	 * to right, the left to right for each level up. See instructions page 3 for details*/
	ArrayList<Node<Integer, ArrayList<RecordId>>> serial_order = null;
	
	public SerializableBPlusTree(SerializableLeafNode root, int order) {
		super(root, order);
		serial_order = this.orderNodes(root);
	}
	
	public SerializableBPlusTree(SerializableIndexNode root, int order) {
		super(root, order);
		serial_order = this.orderNodes(root);
	}
	
	/**
	 * annotate the BPlus tree and create a new tree with serializable nodes
	 * @param tree
	 */
	public SerializableBPlusTree(BPlusTree<Integer, ArrayList<RecordId>> tree){
		super(tree);
		Node<Integer, ArrayList<RecordId>> root = tree.getRoot();
		ArrayList<Node<Integer, ArrayList<RecordId>>> order = this.orderNodes(root);
		this.root = this.annotate(root,order);
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
			System.err.println("Fail to serialize B+ tree: "+this);
			e.printStackTrace();
			return;
		}
		this.serialize(channel);
	}

	@Override
	public void serialize(FileChannel channel) {
		ByteBuffer buffer = null;
		buffer = ByteBuffer.allocate( SystemCatalogue.PAGE_SIZE );
		
		//Write the header page
		//Put in address of the root
		buffer.putInt(this.root.getSerialNumber());
		
		//Put in number of leaves, at offset 4
		buffer.putChar(' ');buffer.putChar(' ');buffer.putChar(' ');
		//TODO:No need to traverse the tree
		int num_of_leaves = 0;
		for(Node<Integer, ArrayList<RecordId>> n:this.serial_order){
			if (n.isLeafNode) num_of_leaves++;
		}
		buffer.putInt(num_of_leaves);
		
		//Put in order of tree, at offset 8
		buffer.putChar(' ');buffer.putChar(' ');buffer.putChar(' ');
		buffer.putInt(this.order);
		
		//Write and clear buffer
		this.writeToChannel(channel, buffer);
		
		//Serialize the nodes in order
		for(Node<Integer, ArrayList<RecordId>> n:this.serial_order){
			if(n.isLeafNode){
				SerializableLeafNode s = new SerializableLeafNode((LeafNode<Integer, ArrayList<RecordId>>)n);
				s.serialize(channel);
			}else{
				SerializableIndexNode s = new SerializableIndexNode((IndexNode<Integer, ArrayList<RecordId>>)n);
				s.serialize(channel);
			}
		}

	}
	
	/**Simple helper to write buffer to a channel and clear that buffer. */
	private void writeToChannel(FileChannel channel, ByteBuffer b){
		b.flip();
		try {
			channel.write(b);
			b.clear();
		} catch (IOException e) {
			System.err.println("Fail to serialize B+ tree: "+this);
			e.printStackTrace();
		}
	}

	/**
	 * Order the nodes of a bplus tree in order for serialization
	 * @param root: the root of a normal BPlus tree
	 * @return: a list of nodes in the order of that will appear in the serialized pages
	 */
	private ArrayList<Node<Integer, ArrayList<RecordId>>> orderNodes(Node<Integer, ArrayList<RecordId>> root){
		//Initialize a queue
		LinkedList<Node<Integer, ArrayList<RecordId>>> queue 
				= new LinkedList<Node<Integer, ArrayList<RecordId>>>();
		ArrayList<Node<Integer, ArrayList<RecordId>>> rev_result 
				= new ArrayList<Node<Integer, ArrayList<RecordId>>>();
		queue.add(root);
		while(queue.peek()!=null){
			Node<Integer, ArrayList<RecordId>> node = queue.poll();
			rev_result.add(node);
			//If the popped node is leaf, do nothing
			//If it's an index node, add all its children to the queue in reverse order
			if(!node.isLeaf()){
				ArrayList<Node<Integer, ArrayList<RecordId>>> children
					= ((IndexNode<Integer, ArrayList<RecordId>>)node).getChildren();
				if(children!=null){
					for(int i = children.size()-1;i>=0;i--){
						queue.add(children.get(i));
					}
				}
			}
		}
		//Reverse the order of result
		Collections.reverse(rev_result);
		return rev_result;
	}
	
	/**
	 * 
	 * @param node: the root of the subtree that needs annotation
	 * @param order: an ordered list of nodes. node n should be annotated with [order.indexOf(n)+1]
	 * @return: the root of the annotated version of the subtree
	 */
	//TODO:fix type specification. It's really ugly
	private SerializableNode<Integer, ArrayList<RecordId>> annotate(
			Node<Integer, ArrayList<RecordId>> node,
			ArrayList<Node<Integer, ArrayList<RecordId>>> order){
		if(node.isLeaf()){
			return new SerializableLeafNode((LeafNode<Integer, ArrayList<RecordId>>)node, order.indexOf(node)+1);
		}
		else{
			//recurse for all the children of node
			ArrayList<Node<Integer, ArrayList<RecordId>>> children
					= ((IndexNode<Integer, ArrayList<RecordId>>)node).getChildren();
			ArrayList<SerializableNode<Integer, ArrayList<RecordId>>> annotated_children 
					= new ArrayList<SerializableNode<Integer, ArrayList<RecordId>>>();
			if(children!=null){
				for(int i = 0; i<children.size() ; i++){
					SerializableNode<Integer, ArrayList<RecordId>> t = this.annotate(children.get(i), order);
					annotated_children.add(t);
				}
			}	
			return new SerializableIndexNode(node.keys, annotated_children, node.getOrder(), order.indexOf(node)+1);
		}
	}

}
