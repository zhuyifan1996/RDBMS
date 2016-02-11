package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Singleton solution for logging large amounts of console output
 * @author Trevor Edwards
 *
 */
public class Logger {
	
	private static Logger sharedInstance;
	public static final String OUTDESTINATION = "logs/";
	public static final String OUTFORMAT = ".txt";
	private PrintStream ps;
	private Calendar gc;
	private SimpleDateFormat sdf;
	
	/**
	 * Private singleton constructor
	 */
	private Logger(){
		File lgdir = new File( OUTDESTINATION);
		lgdir.mkdir();
		FileOutputStream fos;
		gc = Calendar.getInstance();
		SimpleDateFormat tempsdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS");
		try {
			String outf = OUTDESTINATION + tempsdf.format( gc.getTime() ) + OUTFORMAT;
			fos = new FileOutputStream ( outf  );
			ps = new PrintStream( fos );
			System.out.println("Log initialized at: " + outf);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Couldn't initialize logger.");
			e.printStackTrace();
		}
		sdf = new SimpleDateFormat("hh:mm:ss:SS");
	}
	
	/**
	 * Prints to log
	 * @param toLog The string to print
	 */
	public static void println( String toLog ){
		assertInstance();
		sharedInstance.ps.println( sharedInstance.format(toLog)  );
	}
	
	/**
	 * See println for Strings
	 */
	public static void println( Object toLog ){
		assertInstance();
		sharedInstance.ps.println( sharedInstance.format(toLog.toString()) );
	}
	
	/**
	 * Puts the timestamp in front of the log string
	 * @param str the log string
	 * @return the log string with a timestamp
	 */
	private String format( String str ){
		gc = Calendar.getInstance();
		return " ["+sharedInstance.sdf.format(sharedInstance.gc.getTime())+ "]" + str;
	}
	
	/**
	 * Helper method for confirming a Logger is setup
	 */
	private static void assertInstance(){
		if ( sharedInstance == null){
			sharedInstance = new Logger();
			println("INITIALIZING LOGGER");
		}
	}

	/**
	 * Prints warnings to console and log for easy notice
	 * @param string The warning message (without WARNING)
	 */
	public static void warnln(String string) {
		System.err.println("WARNING: "+string);
		println("WARNING: " + string);
		
	}

}
