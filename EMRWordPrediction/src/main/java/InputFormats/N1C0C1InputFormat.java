package InputFormats;

import ProbabilityParameters.ProbabilityParameters;
import RecordReaders.N1C0C1RecordReader;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class N1C0C1InputFormat  extends AbstractTrigramInputFormat {
    @Override
    public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new N1C0C1RecordReader();
    }
}
