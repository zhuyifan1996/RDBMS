package cs4321test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Runs all of our test cases in one click
 * @author Trevor Edwards
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ 
				P4Provided.class,
				P2Provided.class,
				JoinTest.class,
				SystemCatalogueTest.class,
				BinaryFormatTest.class,
				SchemaTest.class,
				ScanTest.class,
				PlannerTest.class,
				SelectTest.class,
				ProjectTest.class,
				OrderTest.class,
				DistinctTest.class,
				ConverterTest.class,
				P3Provided.class
				}
)
public class AllTests {}