package InputFormats;

import ProbabilityParameters.ProbabilityParameters;
import RecordReaders.C1TrigramRecordReader;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class C1TrigramInputFormat extends AbstractTrigramInputFormat {
    @Override
    public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new C1TrigramRecordReader();
    }
}
