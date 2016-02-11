package operators.util.bplustree;

/**
 * Represents a B+ tree node that can be serialized.
 * @author Alvin Zhu
 *
 */
public interface SerializableNode<K extends Comparable<K>, T> extends Serializable {
	
	/**
	 * Represent page number of a node or a tree after serialization, see p3 of instructions.
	 * A serializable BPlus tree, has serial_number 0, i.e. the header page.
	 */
	public int getSerialNumber();
	
	public void setSerialNumber(int n);
}
