package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import operators.physical.PhysicalOperatorUtil;
import operators.physical.ScanOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import main.Logger;
import main.Main;
import db.Schema;
import db.SchemaWithDictionary;
import db.SystemCatalogue;
import db.Tuple;

/**
 * Tests the ScanOperator, our most basic operator.
 * @author Trevor Edwards
 *
 */
public class ScanTest {

	private TestAux testAux = new TestAux();
	
	@Before
	public void setUp() throws Exception {
		Main.reset();
		SystemCatalogue.setSetInputFormat(".csv");
		Main.INPUT_BINARY_FORMAT = false;
		Main.OUTPUT_BINARY_FORMAT = false;
		Main.fullySorted = true;
		testAux = new TestAux();
		testAux.ignoreTestsArray.add("README");
	}

	@After
	public void tearDown() throws Exception {
		Main.reset();
	}
	
	/**
	 * Select Boats, see if the data is there.
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@Test
	public void test() throws IOException, ParseException {

		Logger.println("Running scan test");
		Table tab = new Table("Sailors","Sailors");
		ScanOperator so = operatorFor("test/scan/input","test/scan/input/db/schema.txt",tab);
		Tuple tuppy = so.getNextTuple();

		Schema sc = SystemCatalogue.getSharedInstance().getSchemaForTable( tab );
		//assertEquals( tuppy.getSchema(), sc );
		
		//This is the SID which should be 1 for the first sailor assuming normal ordering
		assertTrue( 1 == (Integer) tuppy.getDataForColumn( sc.getColumns()[0]));
		assertTrue( 200 == (Integer) tuppy.getDataForColumn( sc.getColumns()[1]));
		assertTrue( 50 == (Integer) tuppy.getDataForColumn( sc.getColumns()[2]));

		Tuple[] flushed = PhysicalOperatorUtil.arrayDump(so);
		assertEquals(5, flushed.length);

		//Test reset
		so.reset();
		flushed = PhysicalOperatorUtil.arrayDump(so);
		assertEquals(6, flushed.length);

		so.reset();
		PhysicalOperatorUtil.arrayDump(so);
	}

	/**
	 * Returns a ScanOperator that can be used for testing and sets up
	 * the system catalog for the table
	 * @throws IOException 
	 */
	protected static ScanOperator operatorFor(String dataPath, String schemaPath, Table tab) throws IOException{
		SystemCatalogue s = SystemCatalogue.setupSharedInstance(dataPath, "tmp");

		Logger.println(dataPath);
		SystemCatalogue.setupSharedInstance( dataPath, "tmp" );

		// set the schema for the current scan
		Schema schema = (SchemaWithDictionary) s.getSchemaForTable(tab);

		ScanOperator so = new ScanOperator(tab, schema);
		return so;
	}

}
