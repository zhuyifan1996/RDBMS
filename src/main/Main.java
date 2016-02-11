package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import db.BufferTupleReader;
import db.BufferTupleWriter;
import db.CustomColumn;
import db.Schema;
import db.SimpleTupleReader;
import db.SimpleTupleWriter;
import db.SystemCatalogue;
import db.TupleReader;
import db.TupleWriter;
import planner.SqlStatementVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import operators.util.bplustree.BPlusTreeBulkLoader;

/**
 * This is the main class that set up the system
 * It interpret the command line arguments, reads input and compute the output.
 * @author Guandao Yang
 */
public class Main {
	// Configuration for tests 
	public static boolean setSortConfig = false; // determine whether override the config file
	public static int sortNum;
	public static int sortBuffers;	
	public static boolean setJoinConfig = false;	
	public static int joinNum;
	public static int joinBuffers;
	
	// flag on whether we should have the tuple fully sorted on all fields
	public static boolean fullySorted = false;
	
	//flag indicating whether to build index
	public static boolean SHOULD_BUILD_INDEX = false;
	
	//flag indicating whether to use index when evaluating queries
	public static boolean SHOULD_USE_INDEX = false;
	
	// whether we want to use binary format
	public static boolean INPUT_BINARY_FORMAT	= true;
	public static boolean OUTPUT_BINARY_FORMAT 	= true;
	
	// Directory configurations
	public static final String DEFAULT_CONFIG_PATH = "interpreter_config_file.txt";
	public static final String DEFAULT_INPUT_DIRECTORY  = "input";
	public static final String DEFAULT_OUTPUT_DIRECTORY = "output";
	public static final String DEFAULT_TEMP_DIRECTORY 	= "tmp";
	private static String TMP = "tmp";
	
	
	/**
	 * Reset options to clean up the statics variables every time before it runs
	 */
	public static void reset(){
		Main.setJoinConfig = false;
		Main.setSortConfig = false;
		Main.INPUT_BINARY_FORMAT = true;
		Main.OUTPUT_BINARY_FORMAT = true;
		SystemCatalogue.setSetInputFormat("");
		
		// erase current tmp
		cleanUpTmpDirRecursively(TMP);
	}
	
	public static void main(String[] args) {
		//Setup IOT
		String inputPath , outFile , tmpPath = null;
		if(args.length > 1){
			//support for old test cases
			inputPath = (args.length > 0 ) ? args[0] : DEFAULT_INPUT_DIRECTORY;
			outFile 	 = (args.length > 1 ) ? args[1] : DEFAULT_OUTPUT_DIRECTORY;
			tmpPath   = (args.length > 2 ) ? args[2] : DEFAULT_TEMP_DIRECTORY;
			
		} else{
			String configFile = (args.length > 0 ) ? args[0] : DEFAULT_CONFIG_PATH;
			System.out.println(configFile);
			Scanner sc = null;
			try {
				sc = new Scanner(new File(configFile));
			} catch (FileNotFoundException e) {
				Logger.warnln("Invalid config file path");
			}
			inputPath = sc.nextLine();
			System.out.println(inputPath);
			outFile = sc.nextLine();
			tmpPath = sc.nextLine();
			SHOULD_BUILD_INDEX = sc.nextInt()==1;
			SHOULD_USE_INDEX = sc.nextInt()==1;
			sc.close();
			
		}
		
		// make sure the temporary directory exists and is clean
		cleanUpTmpDirRecursively(tmpPath);
		
		
		try {		
			SystemCatalogue.setupSharedInstance(inputPath, tmpPath);
			TMP = tmpPath;
			
			if(SHOULD_BUILD_INDEX){
				Scanner sc = new Scanner(new File(inputPath + "/db/index_info.txt"));
				while(sc.hasNext()){
					String reln = sc.next();
					String col = sc.next();
					boolean clustered = sc.nextInt() == 1;
					int order = sc.nextInt();
					Table tab = new Table();
					tab.setName(reln);
					Schema schem = SystemCatalogue.getSharedInstance().getSchemaForTable(tab);
					CustomColumn c = null;
					for( CustomColumn schemcol : schem.getColumns()){
						if(schemcol.getName().equals(col))
							c = schemcol;
					}
					System.out.println(schem + "sdfsd");
					if( c == null) Logger.warnln("Tried to build index on unknown column");
					BPlusTreeBulkLoader bptbl = 
							new BPlusTreeBulkLoader( new File(inputPath + "/db/data/"+reln), c, schem, order, clustered);
					bptbl.bulkLoad();
				}
				sc.close();
			}
			
		
			CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath+"/queries.sql"));

			Statement statement;
			int counter = 1;
			while ((statement = parser.Statement()) != null) {
				if(SHOULD_USE_INDEX){
				//TODO: USE index here
				}
				evaluateStatement( statement, outFile, tmpPath, counter );
				counter++;
				
				// clean up the temporary directory
				cleanUpTmpDirRecursively(tmpPath);
			}
		} catch (Exception e) {
			System.err.println("Exception occurred while loading.");
			cleanUpTmpDirRecursively(tmpPath);
			e.printStackTrace();
		}
	}
	
	/**
	 * Recursively clean up all the directly under level dir
	 * @param dir
	 */
	public static void cleanUpTmpDirRecursively(String dir){	
		if (dir == null) return;
		File tmp = new File(dir);
		if(tmp.list() == null) return;
		for (String file:tmp.list()){
			File newFile = new File(tmp.getAbsolutePath()+"/"+file);
			if (newFile.exists() && newFile.isDirectory()){
				cleanUpTmpDirRecursively(tmp.getAbsolutePath()+"/"+file);
			}
			newFile.delete();
		}		
	}

	/**
	 * Factors out evaluation process
	 * @param statement The statement to process
	 * @param out The filename for output
	 */
	public static void evaluateStatement(Statement statement, String outFile, String tmpDir, int counter){
		
		try{
			// construct the output
			File f = new File(outFile+"/query"+counter);
			Logger.println("Evaluating query" + counter );
			SqlStatementVisitor sVisitor = new SqlStatementVisitor(f);
			statement.accept(sVisitor);
			
			// clean up the temporary directory after each query evaluation
			(new File(tmpDir)).delete();
			(new File(tmpDir)).mkdir();
			Logger.println("Finished evaluating query " + counter);
		} catch(Exception e){
			Logger.println("Encountered a terminating exception in processing query " + counter );
			e.printStackTrace();
		} catch(Error e){
			Logger.println("Encountered a terminating error in processing query " + counter );
			e.printStackTrace();
		}

	}
	
	/**
	 * Update the Join Configuration (for test purpose)
	 * @param ops
	 */
	public static void setJoinOptions(int[] ops){
		Main.joinNum = ops[0];
		if( Main.joinNum == 1 ){
			Main.joinBuffers = ops[1];
		}
		Main.setJoinConfig = true;
	}
	
	/**
	 * Update the Sort Configuration (for testing purpose)
	 * @param ops
	 */
	public static void setSortOptions(int[] ops){
		Main.sortNum = ops[0];
		if( Main.sortNum == 1){
			Main.sortBuffers = ops[1];
		}
		Main.setSortConfig = true;
	}
	
	/**
	 * Reader factory;
	 * @param file
	 * @param scma
	 * @return the reader according to system configuration
	 */
	public static TupleReader tupleReaderFactory(File file, Schema scma){
		if (Main.INPUT_BINARY_FORMAT){
			return new BufferTupleReader(file, scma, SystemCatalogue.PAGE_SIZE);	
		}else{
			return new SimpleTupleReader(file, scma);
		}		
	}
	
	/**
	 * Writer Factory
	 * @param file
	 * @param scma
	 * @return the default writer according to system configuration
	 */
	public static TupleWriter tupleWriterFacotry(File file, Schema scma){
		if (Main.OUTPUT_BINARY_FORMAT){
			return new BufferTupleWriter(file, scma, SystemCatalogue.PAGE_SIZE);	
		}else{
			TupleWriter writer = new SimpleTupleWriter(file);
			writer.setOutputSchema(scma);
			return writer;
		}	
	}
}
