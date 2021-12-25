package InputFormats;

import ProbabilityParameters.ProbabilityParameters;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public abstract class AbstractTrigramInputFormat extends FileInputFormat<AbstractTrigram, ProbabilityParameters> {

        @Override
        abstract public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader (InputSplit split, TaskAttemptContext context);

        @Override
        protected boolean isSplitable(JobContext context, Path file) {
            CompressionCodec codec =
                    new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
            return codec == null;
        }

}
