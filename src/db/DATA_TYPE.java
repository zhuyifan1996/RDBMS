package db;

public enum DATA_TYPE {
	NULL		(0),	// NULL value
	LONG 		(1), 	// for integer value !will be represented asan integer
	BOOL		(2),	// Boolean Type
	DOUBLE 		(3),	// Float value	
	DATE    	(4),	// Date value
	TIME		(5),	// Time value
	TIMESTAMP	(6),	// Time stamp
	STRING  	(7);	// String value
	
	private final int index; // index of the data type
	DATA_TYPE(int index) {
		this.index = index;
    }
	
	public int getIndex() {
		return index;
	}
}
