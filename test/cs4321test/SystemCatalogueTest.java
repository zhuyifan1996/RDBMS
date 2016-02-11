package cs4321test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import db.SystemCatalogue;
import main.Logger;

/**
 * Tests basic functionality of SystemCatalogue
 * @author Trevor Edwards
 *
 */

/**
 * Basic testing for our SystemCatalogue
 * @author Trevor Edwards
 *
 */
public class SystemCatalogueTest {
	
	//TODO These tests are lacking can we add more
	@Test
	public void isSingletonTest() throws IOException{
		Logger.println("Running syscat test");
		SystemCatalogue s1 = SystemCatalogue.setupSharedInstance("test/scan/input", "tmp");
		SystemCatalogue s2 = SystemCatalogue.getSharedInstance();
		assertEquals(s1,s2);
	}

}
