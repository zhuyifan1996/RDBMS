package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import main.Logger;
import net.sf.jsqlparser.schema.Table;

/**
 * System Catalogue class is a singleton that contains 
 * all meta information about the database system
 * @author Guandao Yang and Trevor Edwards
 *
 */
public class SystemCatalogue {

	/**
	 * schemaInfo stores all table name to table schema mapping
	 */
	private HashMap<String, Schema> schemaInfo;
	private static SystemCatalogue sharedInstance;
	private static final String SOURCE_PATH = "/db/data/";
	private static final String SCHEMA_PATH = "/db/schema.txt";
	private static String INFILEFORMAT = "";
	private static String tempPath = "/tmp";
	private static String inputPath;
	
	/**
	 * How many bytes does a page contains
	 */
	public static final int PAGE_SIZE = 4096;
	
	/**
	 * Singleton Initializer
	 * PRECONDITION: setupSharedInstance was already called
	 * @return the SystemCatalogue singleton
	 * @throws IOException 
	 */
	public static SystemCatalogue getSharedInstance() throws IOException{
		if (SystemCatalogue.sharedInstance == null){
			assert(false); //We expect the clients to obey the precondition
			SystemCatalogue.sharedInstance = new SystemCatalogue(null, null);
			Logger.warnln("System catalogue improperly initialized");
		}
		
		return SystemCatalogue.sharedInstance;
	}
	
	/**
	 * Singleton Initializer for setting up the input path
	 * @param inputPath the path to the input directory
	 * return the SystemCatalogue singleton
	 * @throws IOException
	 */
	public static SystemCatalogue setupSharedInstance(String inputPath, String tmpPath) throws IOException{
		if (SystemCatalogue.sharedInstance == null){
			SystemCatalogue.sharedInstance = new SystemCatalogue( 
				inputPath != null? inputPath 	: SystemCatalogue.inputPath, 
				tmpPath	  != null? tmpPath		: SystemCatalogue.tempPath
			);
		}else{
			SystemCatalogue.inputPath 	= inputPath != null	? inputPath	: SystemCatalogue.inputPath;
			SystemCatalogue.tempPath 	= tmpPath != null	? tmpPath	: SystemCatalogue.tempPath;
		}
		
		// read the default schema file
				FileReader fr = new FileReader(inputPath+SCHEMA_PATH);
				sharedInstance.readSchema(fr);	
		
		return SystemCatalogue.sharedInstance;
	}
	
	/**
	 * Hidden initialization method.
	 * @throws IOException 
	 */	
	private SystemCatalogue( String inputPath , String tmpPath) throws IOException{
		SystemCatalogue.inputPath = inputPath;
		this.schemaInfo = new HashMap<String, Schema>();
		
		// read the default schema file
		FileReader fr = new FileReader(inputPath+SCHEMA_PATH);
		this.readSchema(fr);		
		
		SystemCatalogue.tempPath = tmpPath+"/";
	}	

	/**
	 * @return return the temp path for the DBMS
	 */
	public static String getTempPath(){
		return SystemCatalogue.tempPath;
	}
	
	/**
	 * Returns the table, in the tableNames, that contains a column with name col.
	 * @param col The column to whose table is to be found
	 * @param tableNames The set of names for potential names for the table
	 * @return the table or null if such a table cannot be found
	 */
	public Table tableForColumn(String col, Set<String> tableNames){
		Iterator<Map.Entry<String, Schema>> it = schemaInfo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Schema> pair=it.next();
			for (CustomColumn c: pair.getValue().getColumns()){
				if(tableNames.contains(c.getTable().getName()) && c.getName().equals(col)) {
					Table t=new Table();
					t.setName(pair.getKey());
					return t;
				}
			}
		}
		return null;
	}
	
	
	
	/**
	 * This function reads the schema from an input stream into the system catalog
	 * @param in
	 * @throws IOException 
	 * @return The schemainfo hashmap after reading (probably shouldn't be used)
	 * TODO, when the schema file gets bigger should be replaced by another parser
	 */
	public HashMap<String,Schema> readSchema(FileReader fr) throws IOException{
		this.schemaInfo = new HashMap<String,Schema>();
		BufferedReader in = new BufferedReader(fr);
		
		String tmp;
		while((tmp  = in.readLine())!= null){

			// now parse the schema 
			String[] scheInfo = tmp.trim().split(" ");
			
			// first string should be the table name
			String tableName = scheInfo[0];

			// parse a sequence of data field
			CustomColumn[] tmpCol = new CustomColumn[scheInfo.length - 1];
			for (int i = 1; i < scheInfo.length; i++){
				tmpCol[i - 1] = new CustomColumn(scheInfo[i], DATA_TYPE.LONG, tableName);
			}
 
			// save the table information
			Schema newSchema = new SchemaWithDictionary(tmpCol);
			this.schemaInfo.put(tableName, newSchema);
		}
		return this.schemaInfo;
	}
	
	/**
	 * Get the schema for a table called Name
	 * @param 	tableName the name for the table where you want to get schema for.
	 * @return 	the schema for corresponding table, null if doesn't exist;
	 */
	public Schema getSchemaForTable(Table t){
		Schema sc = this.schemaInfo.get(t.getName());
		//If this assertion fails, we do not have a schema for this table
		assert( sc != null );
		if( sc == null){
			System.out.println(schemaInfo.keySet());
			Logger.warnln("Missing schema for " + t);
			
		}
		return sc;
	}
	
	/**
	 * Gets a stream referring to the file for a given table
	 * @param tableName the name of the table
	 * @return 	The input stream from which the data of 
	 * 			the table [tableName] can be read 
	 * @throws FileNotFoundException 
	 */
	public FileInputStream getSourceForTable(Table t) throws FileNotFoundException{
		return new FileInputStream(formatInputFile(t.getName()));
	}
	
	/**
	 * Get a file for the specific tables
	 * @param t The table to be formatted
	 * @return The file
	 */
	public File getFileForTable(Table t){
		return new File(formatInputFile(t.getName()));
	}
	
	/**
	 * Formats a table's name for file retrieval based on the input source
	 */
	private String formatInputFile(String fname ){
		return inputPath+SOURCE_PATH+fname+INFILEFORMAT;
	}
	
	/**
	 * Change the format of the input file
	 * @param format
	 */
	public static void setSetInputFormat(String format){
		INFILEFORMAT = format;
	}

	/**
	 * @return return the input path
	 */
	public String getInputPath() {
		return inputPath;
	}

}
