package cs4321test;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;

import db.Schema;
import db.SystemCatalogue;
import main.Logger;
import net.sf.jsqlparser.schema.Table;

import org.junit.Test;

/**
 * Tests the functionality of Schema
 * @author Trevor Edwards
 *
 */
public class SchemaTest {

	@Test
	public void testColumnSetup() throws IOException {
		Logger.println("Running schema test");
		
		SystemCatalogue s = SystemCatalogue.setupSharedInstance("test/scan/input","tmp");
		FileReader fr = new FileReader("input/db/schema.txt");
		s.readSchema(fr);
		
		//TODO: Test this more thoroughly
		Schema sc = s.getSchemaForTable( new Table("Sailors","Sailors") );
		assertTrue( sc.getColumns().length == 3);
		Schema sc1 = s.getSchemaForTable( new Table("Boats","Boats") );
		assertTrue( sc1.getColumns().length == 3);
		Schema sc2 = s.getSchemaForTable( new Table("Reserves","Reserves") );
		assertTrue( sc2.getColumns().length == 2);
		assertTrue(sc.hasColumn(sc.getColumns()[2]));
		assertFalse(sc.hasColumn(sc2.getColumns()[1]));
		assertTrue(sc.getColumnIndex( sc.getColumns()[0] ) == 0);
	}

}
