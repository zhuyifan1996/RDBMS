package cs4321test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import db.BufferTupleReader;
import db.BufferTupleWriter;
import db.SimpleTupleReader;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleReader;

public class BinaryFormatTest {
	// Files and Paths
	private String b2h_path = "test/format/b2h/"; 
	private File b2h = new File("test/format/b2h/");
	
	private String b_path = "test/format/binary/"; 
	private File b = new File("test/format/binary/");
	
	private String h_path = "test/format/humanreadable/"; 
	private File h = new File("test/format/humanreadable/");
	
	private String h2b_path = "test/format/h2b/";
	private File h2b = new File("test/format/h2b/");
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testReaderReset(){
		// how to test the tuple reader reset
		
	}

	@Test
	public void testBinaryWriter() throws IllegalArgumentException, IOException {
		// read from the simple tuple writer
		// write into binary (h2b), then compare the binary output in tuples
		
		// round 1, read and write
		for (String inFileName:this.h.list()){
			File outFile = new File(this.h2b_path + inFileName);
			File inFile = new File(this.h_path + inFileName);
			BufferTupleWriter writer = new BufferTupleWriter(outFile, null, SystemCatalogue.PAGE_SIZE);
			SimpleTupleReader reader = new SimpleTupleReader(inFile, null);
			
			while(reader.hasNextTuple()){
				writer.writeTuple(reader.getNextTuple());
			}
			writer.close();
			reader.close();
		}
		
		// now compare two results
		boolean pass = true;
		String cumulatedMessage = "Failed at:\n";
		
		for (String inFileName:this.h2b.list()){
			File h_inFile = new File(this.h_path + inFileName);
			File b_inFile = new File(this.h2b_path + inFileName);
			BufferTupleReader testing  = new BufferTupleReader(b_inFile, SystemCatalogue.PAGE_SIZE);
			SimpleTupleReader expected = new SimpleTupleReader(h_inFile, null);
			
			String msg = this.testTwoReaders(inFileName, testing, expected);
			if (!msg.equals("")){
				cumulatedMessage = cumulatedMessage + msg;
				pass = false;
			}
			testing.close();
			expected.close();
		}
		
		if (!pass){
			fail(cumulatedMessage);
		}
	}
	
	@Test
	public void testBinaryReader() {
		// read from binary, write to b2h	
		boolean pass = true;
		String cumulatedMessage = "Failed at:\n";
		String[] binaries = new File(this.b_path).list();
		for (String inFileName:binaries){
			File h_inFile = new File(this.h_path + inFileName);
			File b_inFile = new File(this.b_path + inFileName);
			BufferTupleReader testing  = new BufferTupleReader(b_inFile, SystemCatalogue.PAGE_SIZE);
			SimpleTupleReader expected = new SimpleTupleReader(h_inFile, null);
			
			String msg = this.testTwoReaders(inFileName, testing, expected);
			if (!msg.equals("")){
				cumulatedMessage = cumulatedMessage + msg;
				pass = false;
			}
			testing.close();
			expected.close();
		}
		
		if (!pass){
			fail(cumulatedMessage);
		}
	}
	
	/**
	 * Test whether the two reader returns the identical results. If so, reutrn "",
	 * other wise, return the detailed error message of mismatches.
	 * @param name
	 * @param testing
	 * @param expected
	 * @return
	 */
	public String testTwoReaders(String name ,TupleReader testing, TupleReader expected){
		String errorMessage = "";
		int count = 0;
		while(testing.hasNextTuple() && expected.hasNextTuple()){
			count++;
			Tuple test = testing.getNextTuple();
			Tuple ans  = expected.getNextTuple();
			if (!test.equals(ans)){
				errorMessage = errorMessage +"File "+name+" isn't equaled at tuple "+ count +"; expected:"+ans+"; got:"+test+"\n";
				break;
			}
		}
		
		if (testing.hasNextTuple()){
			errorMessage = errorMessage +"File test has extra column\n";			
		}
		
		if (testing.hasNextTuple()){
			errorMessage = errorMessage +"File test doesn't have enough column\n";
		}
		
		return errorMessage;	
	}

}
