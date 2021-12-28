package InputFormats;

import ProbabilityParameters.ProbabilityParameters;
import RecordReaders.C0N1N2TrigramRecordReader;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class C0N1N2TrigramInputFormat extends AbstractTrigramInputFormat {
    @Override
    public RecordReader<AbstractTrigram, ProbabilityParameters> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new C0N1N2TrigramRecordReader();
    }
}
