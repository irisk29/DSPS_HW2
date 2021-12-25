package InputFormats;

import RecordReaders.N2TrigramRecordReader;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class N2TrigramInputFormat extends AbstractTrigramInputFormat {
    @Override
    public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new N2TrigramRecordReader();
    }
}
