package planner;

import java.io.File;

import main.Logger;
import main.Main;

import java.io.IOException;

import db.BufferTupleWriter;
import db.SimpleTupleWriter;
import db.SystemCatalogue;
import db.Tuple;
import db.TupleWriter;
import operators.physical.PhysicalOperator;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

/**
 * This is the Naive implementation of the SQL Planner
 * that reads the parsed tree and build up the Operation Tree.
 * 
 * @author Guandao Yang
 */
public class SqlStatementVisitor implements StatementVisitor {

	//The output stream if the statement is going to write anything.
	private File file;
	private TupleWriter writer;	

	/**
	 * Initialize with the provided tuple writer
	 * @param out
	 */
	public SqlStatementVisitor(File outFile){
		this.file = outFile;
		this.setWriterWithFile(this.file);
	}

	/**
	 * This will use the file to set the tuple writer for each visit. 
	 * Reset will clear the current tuple writer, therefore please
	 * set the tuple writer every time before the visitor is used.
	 * 
	 * @param outFile
	 */
	public void setWriterWithFile(File outFile){
		this.writer = Main.OUTPUT_BINARY_FORMAT ? 
						new BufferTupleWriter(this.file, null, SystemCatalogue.PAGE_SIZE) : 
						new SimpleTupleWriter(this.file);
	}

	/**
	 * @return fetch the current tuple writer
	 */
	public TupleWriter getTupleWriter(){
		return this.writer;
	}


	@Override
	public void visit(Select arg0) {
		Logger.println( "Processing a new query" );
		SqlSelectVisitor sVisitor = new SqlSelectVisitor();
		arg0.getSelectBody().accept(sVisitor);

		// Output what we have
		PhysicalOperator queryOp = sVisitor.getOperator();
		outputTuplesFromOperator(queryOp);	

	}

	/**
	 * Output the information we got from the query.
	 * If the queryOp is null, will do nothing.
	 * @param queryOp
	 */
	private void outputTuplesFromOperator(PhysicalOperator queryOp){
		if (queryOp != null){			
			Tuple t = null;
			// output
			try {
				while((t = queryOp.getNextTuple()) != null){
					this.writer.writeTuple(t);
				}
				this.writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 		
		}
	}

	@Override
	public void visit(Delete arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(Update arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(Insert arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(Replace arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(Drop arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(Truncate arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

	@Override
	public void visit(CreateTable arg0) {
		Logger.println("Attempted to call unimplemented SQL function.");
	}

}
