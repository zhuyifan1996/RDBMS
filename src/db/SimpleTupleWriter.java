package db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import main.Logger;

public class SimpleTupleWriter implements TupleWriter {

	private File file;
	private BufferedWriter writer;
	private Schema schema;

	public SimpleTupleWriter(File file) {
		this.setOutputFile(file);
	}

	@Override
	public File getCurrentFile() {
		return this.file;
	}

	@Override
	public Schema getCurrentSchema() {
		return this.schema;
	}

	@Override
	public void reset() {
		this.setOutputFile(this.file);
	}

	@Override
	public void writeTuple(Tuple tp) throws IllegalArgumentException, IOException {
		if (this.schema != null){
			if (this.schema.isValidData(tp.getData())){
				this.writer.write(tp.toCSVString(","));
			}else{
				throw new IllegalArgumentException("Trying to write an invalid tuple");
			}
		}else{
			this.schema = tp.getSchema();
			writer.write(tp.toCSVString(","));
		}
		
	}

	@Override
	public void setOutputSchema(Schema scma) {
		this.schema = scma;
	}

	@Override
	public boolean setOutputFile(File file) {
		try {
			this.file = file;
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file)));
			return true;
		} catch (FileNotFoundException e) {
			Logger.warnln("File not found in STW");
			e.printStackTrace();
			return false;
		}				
	}

	@Override
	public void close() {
		try {
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() throws IOException {
		this.writer.flush();
	}

}
