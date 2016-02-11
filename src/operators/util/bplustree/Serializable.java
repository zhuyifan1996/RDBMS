package operators.util.bplustree;

import java.io.File;
import java.nio.channels.FileChannel;

/**
 * Represetation of instances about the statics BPlus Tree
 * These instance include: Nodes, Tree, Leaf, Index...
 * @author Guandao Yang
 *
 * @param <K>
 * @param <V>
 */
public interface Serializable {	
	/**
	 * Re-initialize the instance using the data from the File
	 * Will start reading data from the beging of the file
	 * @param in  [precondition] 	[in] is a file with valid serialization 
	 * 								data for the corresponding instance
	 */
	public void deSerialize(File in);
	
	/**
	 * Re-initialize the instance using the data from the channel
	 * (more over, it will start reading data from the current 
	 * position of the file channel)
	 * @param channel	[precondition]	[channel] is a file channel that refers 
	 * 									to a file with correct serialize format
	 * 									Moreover, the current position of the channel  
	 * 									most be the starting point of some page
	 * 									(i.e. n*SystemCatalogue.PAGE_SIZE for some n)
	 */
	public void deSerialize(FileChannel channel);
	
	/**
	 * Will serialize the node into the file
	 * The writing will starting from the beginning of the file.
	 * @param out
	 */
	public void serialize(File out);
	
	/**
	 * Will serialize into the file channel.
	 * The writing will start from the current position of the file channel
	 * (so it will append to the file, erase the thing from current position on)
	 * @param channel	[precondition]	The current condition of the channel
	 * 									must be the starting point of a page
	 * 									(i.e. n*SystemCatalogue.PAGE_SIZE for some n)
	 */
	public void serialize(FileChannel channel);
}
