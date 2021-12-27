package InputFormats;

import ProbabilityParameters.ProbabilityParameters;
import RecordReaders.C0TrigramRecordReader;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class C0TrigramInputFormat extends AbstractTrigramInputFormat {
    @Override
    public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new C0TrigramRecordReader();
    }
}
