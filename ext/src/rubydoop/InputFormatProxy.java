package rubydoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.JobContext;

import org.jruby.runtime.builtin.IRubyObject;


public class InputFormatProxy extends InputFormat<Object, Object> {

  private InstanceContainer instance;
  protected String factoryMethodName;

  public InputFormatProxy() {
    factoryMethodName = "create_input_format";
    instance = new InstanceContainer(factoryMethodName);
  }

  public RecordReader createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
	  String recordReaderClass = context.getConfiguration().get("record_reader_class");
	  RecordReader rr = null;
	  try
	  {
		  rr = (RecordReader) Class.forName(recordReaderClass).newInstance();
	  }
	  catch(Exception e)
	  {
	      throw new IOException("RecordReader class " + recordReaderClass + " could not be found", e);
	  }
	  return rr;
  }

  public List getSplits(JobContext context) throws IOException, InterruptedException {
    instance.setup(context.getConfiguration());

    IRubyObject iro = (IRubyObject) instance.callMethod("get_splits", context);
    List rubySplits = (List) iro.toJava(List.class);
    return rubySplits;
  }
}