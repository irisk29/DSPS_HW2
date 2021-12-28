package RecordReaders;

import ProbabilityParameters.ProbabilityParameters;
import Trigrams.AbstractTrigram;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import java.io.IOException;

public abstract class AbstractTrigramRecordReader extends RecordReader<AbstractTrigram, ProbabilityParameters> {
    protected LineRecordReader reader;
    protected AbstractTrigram key;
    protected ProbabilityParameters value;

    protected abstract AbstractTrigram buildTrigram(String w1, String w2, String w3);

    AbstractTrigramRecordReader() {
        this.reader = new LineRecordReader();
        this.key = null;
        this.value = null;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException {
        this.reader.initialize(split, context);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    protected AbstractTrigram parseTrigram(String str) {
        String[] trigramWords = str.split(",");
        String w1 = trigramWords[0], w2 = trigramWords[1], w3 = trigramWords[2];
        return buildTrigram(w1, w2, w3);
    }

    protected ProbabilityParameters parseProbabilityParameters(String str) {
        String[] probabilityParameters = str.split(",");
        LongWritable N1 = new LongWritable(Long.parseLong(probabilityParameters[0]));
        LongWritable N2 = new LongWritable(Long.parseLong(probabilityParameters[1]));
        LongWritable N3 = new LongWritable(Long.parseLong(probabilityParameters[2]));
        LongWritable C0 = new LongWritable(Long.parseLong(probabilityParameters[3]));
        LongWritable C1 = new LongWritable(Long.parseLong(probabilityParameters[4]));
        LongWritable C2 = new LongWritable(Long.parseLong(probabilityParameters[5]));
        return new ProbabilityParameters(N1, N2, N3, C0, C1, C2);
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (this.reader.nextKeyValue()) {
            String[] keyValue = reader.getCurrentValue().toString().split("\t");
            this.key = parseTrigram(keyValue[0]);
            this.value = parseProbabilityParameters(keyValue[1]);
            return true;
        }
        else {
            this.key = null;
            this.value = null;
            return false;
        }
    }

    @Override
    public AbstractTrigram getCurrentKey() {
        return this.key;
    }

    @Override
    public ProbabilityParameters getCurrentValue() {
        return this.value;
    }

    @Override
    public float getProgress() throws IOException {
        return this.reader.getProgress();
    }
}
