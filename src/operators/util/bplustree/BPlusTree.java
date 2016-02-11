package operators.util.bplustree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import java.util.List;

/**
 * Represents a BPlusTree
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 * @author Amber Hillhouse and Trevor Edwards
 */
public class BPlusTree<K extends Comparable<K>, T>{

	private Node<K,T> root;
	public final int order;

	public BPlusTree(BPlusTree<K,T> other){ 
		this.root = other.root;
		this.order = other.order;
	}
	
	/**
	 * Default initializer
	 * @param root
	 * @param order
	 */
	public BPlusTree(Node<K,T> root, int order){
		this.root = root;
		this.order = order;
	}
	
	/**
	 * @return the root of the tree. 
	 * TODO this is not safe, then the tree can be modified outside!
	 */
	public Node<K,T> getRoot(){
		return root;
	}
	
	/**
	 * Get the the child in an IndexNode that corresponds to the 
	 * current key
	 * @param key : key being searched for
	 * @param node : node to take child from
	 * @return Child from node that corresponds to the key
	 */
	private Node<K,T> getChild(K key, IndexNode<K,T> node) {
		Node<K,T> child = node.children.get(0);
		int index = 0;
		for (K k : node.keys) {
			if (key.compareTo(k) >= 0) {
				child = node.children.get(index+1);
			}
			index++;
		}
		return child;
	}
	
	public T search(K key) {
		if (root != null) {
			Node<K,T> leaf = root;
			while (!leaf.isLeafNode){
				leaf = getChild(key, (IndexNode<K,T>)leaf);
			}

			// go through leaf node and determine if key exists
			int keyIndex = 0;
			for (K k : leaf.keys){
				if (k.equals(key)) {
					return ((LeafNode<K,T>)leaf).values.get(keyIndex);
				}
				keyIndex++;
			}
		}
		
		// key does not exist
		return null;
	}
	
	public Iterator<T> rangeSearch(K lowKey, K highKey) {
		// TODO Auto-generated method stub
		return null;
	}

	//TODO Remove
	public void insert(K key, T value) {
		// Tree has not been initialized
		if (root == null) {
			root = new LeafNode<K,T>(key, value, this.order);
		}
		else {
			insert(key, value, root);
			
			// root node is leaf node and needs to be expanded
			if (root.isLeafNode && root.keys.size() > order*2) {
				Entry<K, Node<K,T>> split = splitLeafNode((LeafNode<K,T>)root);
				root = new IndexNode<K,T>(split.getKey(), root, split.getValue(), this.order);
			}
		}
	}
	
	//TODO Remove
	private void insert(K key, T value, Node<K,T> node) {
		if (node.isLeafNode) {
			((LeafNode<K,T>)node).insertSorted(key, value);
		}
		else {
			Node<K,T> child = getChild(key, (IndexNode<K,T>)node);
			insert(key, value, child);
			
			// After inserted to child, check if child has overflowed
			// If so, then split the child and add new link to 
			// the new part
			if (child.keys.size() > order*2) {
				int childIndex = 
						((IndexNode<K,T>)node).children.indexOf(child);
				 Entry<K, Node<K,T>> split = child.isLeafNode ? 
						 splitLeafNode((LeafNode<K,T>)child) : 
					     splitIndexNode((IndexNode<K,T>)child);
				 ((IndexNode<K,T>)node).insertSorted(split, childIndex); 
			}
			
			// ROOT IS SPECIAL CASE
			if (node == root && node.keys.size() > order*2) {
				Entry<K, Node<K,T>> split = 
						splitIndexNode((IndexNode<K,T>)node);
				IndexNode<K,T> newRoot = new IndexNode<K,T>(split.getKey(),  node, split.getValue(), this.order);
				root = newRoot;
			}
		}
	}

	/**
	 * Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf : leaf to process
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitLeafNode(LeafNode<K,T> leaf) {
		
		// get keys and values for new leaf
		List<K> rightKeys = 
			new ArrayList<K>(leaf.keys.subList(order, leaf.keys.size()));
		List<T> rightValues = 
			new ArrayList<T>(leaf.values.subList(order, leaf.values.size()));
		K splittingKey = rightKeys.get(0);
		LeafNode<K,T> rightLeaf = new LeafNode<K,T>(rightKeys, rightValues, this.order);
		
		// remove keys and values from old leaf
		leaf.keys.subList(order, leaf.keys.size()).clear();
		leaf.values.subList(order, leaf.values.size()).clear();
		
		rightLeaf.nextLeaf = leaf.nextLeaf;
		rightLeaf.previousLeaf = leaf;
		leaf.nextLeaf = rightLeaf;
		
		return 
			new AbstractMap.SimpleEntry<K, Node<K,T>>(splittingKey, rightLeaf);
	}

	/**
	 * split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index : index node to process
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K,T>> splitIndexNode(IndexNode<K,T> index) {
		List<K> rightKeys = 
			new ArrayList<K>(index.keys.subList(order+1, index.keys.size()));
		
		List<Node<K,T>> rightValues = 
			new ArrayList<Node<K,T>>
			(index.children.subList(order+1, index.children.size()));
		
		K splittingKey = index.keys.get(order);
		IndexNode<K,T> rightIndex = new IndexNode<K,T>(rightKeys, rightValues, this.order);
		
		// remove keys and values from old node
		index.keys.subList(order, index.keys.size()).clear();
		index.children.subList(order+1, index.children.size()).clear();
				
		return 
			new AbstractMap.SimpleEntry<K, Node<K,T>>(splittingKey, rightIndex);
	}

	/**
	 * Delete a key/value pair from this B+Tree
	 * 
	 * @param key : key to delete
	 */
	public void delete(K key) {
		if (root == null) {
			//throw new IllegalArgumentException("This tree is empty");
			return;
		}
		
		delete(key, root);
		
		// ROOT IS SPECIAL CASE: If leaf, and leaf is empty, tree is empty
		// If index node and node is empty, replace with a child
		if (root.keys.size() == 0 ) {
			if (root.isLeafNode) {
				root = null;
			}
			else {
				root = ((IndexNode<K,T>)root).children.get(0);
			}
		}
		
	}
	
	/**
	 * Helper function to delete key/value pair from B+Tree
	 * @param key : key to delete
	 * @param node : current node
	 */
	public void delete(K key, Node<K,T> node) {
		if (node.isLeafNode) {
			if (!node.keys.contains(key)) {
				//throw new IllegalArgumentException("This key does not exist");
				return;
			}
			
			((LeafNode<K,T>)node).values.remove(node.keys.indexOf(key));
			node.keys.remove(key);
		}
		else {
			Node<K,T> child = getChild(key, (IndexNode<K,T>)node);
			delete(key, child);
			
			// After deleted from child, check if child has underflowed
			if (child.keys.size() < order) {
				int childIndex = 
						((IndexNode<K,T>)node).children.indexOf(child);
				
				Node<K,T> left = childIndex == 0 ? 
						child : ((IndexNode<K,T>)node).children.get(childIndex-1);
				Node<K,T> right = childIndex == 0 ?
						((IndexNode<K,T>)node).children.get(childIndex+1) : 
						child;
				
				int removeIndex = child.isLeafNode ? 
						handleLeafNodeUnderflow((LeafNode<K,T>)left,
								                (LeafNode<K,T>)right,
								                (IndexNode<K,T>)node) : 
						handleIndexNodeUnderflow((IndexNode<K,T>)left,
								                 (IndexNode<K,T>)right,
								                 (IndexNode<K,T>)node);
				if (removeIndex >= 0) {
					node.keys.remove(removeIndex);
					((IndexNode<K,T>)node).children.remove(removeIndex);
				}
			}			
		}
	}

	/**
	 * Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode<K,T> left, LeafNode<K,T> right,
			IndexNode<K,T> parent) {
		K newKey;
		int parentKeyIndex = parent.children.indexOf(left);
		
		int totalNodes = left.keys.size() + right.keys.size();
		int redistributeCount;

		if (left.keys.size() < order && right.keys.size() > order) {
			// This is how many need to be moved to the left node to 
			// make them even
			redistributeCount = (totalNodes/2) - left.keys.size();
			
			// If left is too small but right has extras
			// remove first size-D from R to keep invariant
			// that at least D are in right
			for (int i = 0; i < redistributeCount; i++) {
				left.insertSorted(right.keys.get(i), right.values.get(i));
			}
			right.keys.subList(0, redistributeCount).clear();
			right.values.subList(0, redistributeCount).clear();
		}
		else if (right.keys.size() < order && left.keys.size() > order) {
			// This is how many need to be moved to the right node to
			// to make them even
			redistributeCount = (totalNodes/2) - right.keys.size();
			int leftStartIndex = left.keys.size() - redistributeCount;
			
			// If right is too small but left has extras
			// remove all from index > D to keep invariant
			// that at least D are in left
			for (int i = leftStartIndex; i < left.keys.size(); i++) {
				right.insertSorted(left.keys.get(i), left.values.get(i));
			}
			left.keys.subList(leftStartIndex, left.keys.size()).clear();
			left.values.subList(leftStartIndex, left.values.size()).clear();			
		}
		else {
			// Always merge into right node
			for (int i = 0; i < left.keys.size(); i++) {
				right.insertSorted(left.keys.get(i), left.values.get(i));
			}
			
			// Set right previous to left previous
			right.previousLeaf = left.previousLeaf;
			
			// Delete key and child at left index
			return parentKeyIndex;
		}
		
		newKey = right.keys.get(0);
		parent.keys.remove(parentKeyIndex);
		parent.keys.add(parentKeyIndex, newKey);
		
		return -1;
	}

	/**
	 * Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode<K,T> leftIndex,
			IndexNode<K,T> rightIndex, IndexNode<K,T> parent) {
		
		int parentKeyIndex = parent.children.indexOf(leftIndex);
		
		int totalNodes = leftIndex.keys.size() + rightIndex.keys.size();
		int redistributeCount;
		
		if (leftIndex.keys.size() < order && rightIndex.keys.size() > order) {
			// This is how many need to be moved into the left node to 
			// make them even
			redistributeCount = (totalNodes/2) - leftIndex.keys.size();
			
			// Rotate keys into parent
			// Move children into left
			for (int i = 0; i < redistributeCount; i++) {
				parent.keys.add(rightIndex.keys.get(i));
				leftIndex.children.add(rightIndex.children.get(i));
			}
			
			// Move keys from parent to left
			for (int i = 0; i < parent.keys.size()-1; i++) {
				leftIndex.keys.add(parent.keys.get(i));
			}
			
			// Remove from right
			rightIndex.keys.subList(0, redistributeCount).clear();
			rightIndex.children.subList(0, redistributeCount).clear();
			
			// Remove from parent
			parent.keys.subList(0, parent.keys.size()-1).clear();
		}
		else if (rightIndex.keys.size() < order && leftIndex.keys.size() > order) {
			// This is how many need to be moved to the right node to
			// to make them even
			redistributeCount = (totalNodes/2) - rightIndex.keys.size();
			int leftStartIndex = leftIndex.keys.size() - redistributeCount;
			
			// Rotate keys into parent
			// Move children into right
			for (int i = leftIndex.keys.size()-1; i >= leftStartIndex; i--) {
				parent.keys.add(0, leftIndex.keys.get(i));
				rightIndex.children.add(leftIndex.children.get(i+1));
			}
			
			// Move keys from parent to right
			for (int i = root.keys.size()-1; i > 0; i--) {
				rightIndex.keys.add(0, parent.keys.get(i));
			}
			
			// Remove from left
			leftIndex.keys.subList(leftStartIndex, leftIndex.keys.size()).clear();
			leftIndex.children.subList(leftStartIndex+1, leftIndex.children.size()).clear();
			
			// Remove from parent
			parent.keys.subList(1, parent.keys.size()).clear();
			
		}
		else {
			// Pull down from parent
			rightIndex.keys.add(0, parent.keys.get(parentKeyIndex));
			
			// Always merge into right node
			// Merge keys over
			for (int i = leftIndex.keys.size()-1; i >= 0; i--) {
				rightIndex.keys.add(0, leftIndex.keys.get(i));
			}
			
			// Merge children over
			for (int i = leftIndex.children.size()-1; i >= 0; i--) {
				rightIndex.children.add(0, leftIndex.children.get(i));
			}
			
			// Delete key and child at left index
			return parentKeyIndex;
		}
		
		return -1;
	}

	/**
	 * @return The order of the tree
	 */
	public int getOrder() {
		return this.order;
	}


}
