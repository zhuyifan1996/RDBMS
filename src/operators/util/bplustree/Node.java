package operators.util.bplustree;
import java.util.ArrayList;

/**
 * Abstract class representing any node of a BPlusTree
 * @author Amber Hillhouse and Trevor Edwards
 *
 * @param <K> the key
 * @param <T> the value
 */
public abstract class Node<K extends Comparable<K>, T>{
	protected boolean isLeafNode;
	protected ArrayList<K> keys;
	private final int order;
	
	public Node(int order){
		this.order = order;
	}
	
	public Node(Node<K,T> other){
		this.order = other.order;
		this.isLeafNode = other.isLeafNode;
		//TODO: does this need to be copied?
		this.keys= other.keys;
	}

	public boolean isOverflowed() {
		return keys.size() > 2 * this.order;
	}

	public boolean isUnderflowed() {
		return keys.size() < this.order;
	}
	
	/**
	 * @return The order of the node
	 */
	public int getOrder(){
		return this.order;
	}

	public boolean isLeaf(){
		return this.isLeafNode;
	}

}
