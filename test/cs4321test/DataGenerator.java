package cs4321test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import db.BufferTupleWriter;
import db.Schema;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleWriter;

/**
 * A nifty class for generating tuples to files
 * TODO: Implement turning tuples to bytecode and writing them to files
 * @author Trevor Edwards
 *
 */
public class DataGenerator {
	
	private static DataGenerator sharedInstance;
	private Random rnd;
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		//easy console for data gen
		Scanner in = new Scanner( System.in );
		String OUTFILE = "test/generated/";
		String DEFSCHEM = "test/generated/db/schema.txt";
		String DEFNAME = "Testing";
		String TMPNAME = "test/generated/tmp/";
		
		SystemCatalogue s = SystemCatalogue.setupSharedInstance(OUTFILE, TMPNAME);
		
		System.out.println("Schema path");
		String nl = in.nextLine();
		String spath = (nl.equals("")) ? DEFSCHEM : nl;
		System.out.println("Schema name (EX: SAILORS)");
		String sname = in.nextLine();
		sname = (sname.equals("")) ? DEFNAME : sname;
		System.out.println("Seed (int at least 1)");
		int seed = in.nextInt();
		System.out.println("Number of tuples");
		int num = in.nextInt();
		
		Schema sc = SystemCatalogue.getSharedInstance().readSchema( new FileReader( new File(spath)) ).get(sname);
		
		File out = new File( OUTFILE + sname + "gen" + seed + "seed" + num + "tup" );
		writeRandomData( out, seed, num, sc );
	}
	
	/**
	 * Private singleton constructor
	 */
	private DataGenerator(){
		rnd = new Random();
	}
	
	
	/**
	 * Gets a randomly generated tuple
	 * @param sc The schema to comply with
	 * @param seed Data randomly generated from [0,seed)
	 * @return the tuple
	 */
	public static Tuple getTuple(Schema sc, int seed){
		assertInstance();
		Integer[] rands = new Integer[sc.getColumns().length];
		for( int i = 0; i < rands.length; i++){
			rands[i] = (int) sharedInstance.rnd.nextInt( seed );
		}
		
		return new Tuple(sc, rands);
	}
	
	public static void writeRandomData(File destination, int seed, int amount, Schema sc) throws IllegalArgumentException, IOException{
		TupleWriter stw = new BufferTupleWriter(destination,sc, SystemCatalogue.PAGE_SIZE);
		for( int i = 0; i < amount; i++){
			Tuple t = DataGenerator.getTuple(sc, seed);
			stw.writeTuple( t );
			
		}
		stw.close();
		
	}
	
	/**
	 * Helper method for confirming a DataGenerator is setup
	 */
	private static void assertInstance(){
		if ( sharedInstance == null){
			sharedInstance = new DataGenerator();
		}
	}

}
