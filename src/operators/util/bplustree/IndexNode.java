package operators.util.bplustree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Represents an index node of a BPlusTree
 * @author Amber Hillhouse and Trevor Edwards
 *
 * @param <K> The key
 * @param <T> The value
 */
public class IndexNode<K extends Comparable<K>, T> extends Node<K,T> {

	// m nodes
	protected ArrayList<Node<K,T>> children; //m+2 children

	public ArrayList<Node<K,T>> getChildren(){
		return this.children;
	}
	
	/**
	 * Default constructor*/
	public IndexNode(int order){
		super(order);
	}
	
	public IndexNode(K key, Node<K,T> child0, Node<K,T> child1, int order) {
		super(order);
		isLeafNode = false;
		keys = new ArrayList<K>();
		keys.add(key);
		children = new ArrayList<Node<K,T>>();
		children.add(child0);
		children.add(child1);
	}

	public IndexNode(List<K> newKeys, List<Node<K,T>> newChildren, int order) {
		super(order);
		
		isLeafNode = false;
		keys = new ArrayList<K>(newKeys);
		children = new ArrayList<Node<K,T>>(newChildren);

	}

	/**
	 * insert the entry into this node at the specified index so that it still
	 * remains sorted
	 * 
	 * @param e
	 * @param index
	 */
	public void insertSorted(Entry<K, Node<K,T>> e, int index) {
		K key = e.getKey();
		Node<K,T> child = e.getValue();
		if (index >= keys.size()) {
			keys.add(key);
			children.add(child);
		} else {
			keys.add(index, key);
			children.add(index+1, child);
		}
	}
}
