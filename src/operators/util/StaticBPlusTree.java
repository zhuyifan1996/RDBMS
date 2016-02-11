package operators.util;

import java.io.File;
import java.util.Iterator;

import db.TupleReader;

public interface StaticBPlusTree<K extends Comparable<?>, V> {
	
	public StaticBPlusTree<K, V> bulkLoad(TupleReader reader, int order, boolean clustered);
	
	public StaticBPlusTree<K, V> bulkLoad(File file, int order, boolean clustered);
	
	public boolean isClustered();
	
	public V search(K key);
	
	public Iterator<V> rangeSearch(K lowKey, K highKey);
	
	public int getOrder();
	
	public StaticBPlusTree<K, V> deSerialize(File out);	
	public void serialize(File out);	
}
