package MapReduceSteps;

import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN3C1C2;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class N3C1C2Counter {

    public static class MapperClass extends Mapper<LongWritable, Text, TrigramN3C1C2, LongWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException,  InterruptedException {
            // gets the three hebrew words
            String[] info = value.toString().split("\\s+");
            String w1Str = info[0];
            String w2Str = info[1];
            String w3Str = info[2];
            // gets the number of times this n-gram appeared in this year
            long amountOfTrigram = Long.parseLong(info[4]);
            LongWritable trigramAmount = new LongWritable(amountOfTrigram);
            // create the <w1,w2,w3> key
            TrigramN3C1C2 trigram = new TrigramN3C1C2(w1Str, w2Str, w3Str);
            // create the <w1,w2,~> key
            TrigramN3C1C2 w1w2 = new TrigramN3C1C2(trigram.getW1(), trigram.getW2(), "~");
            // create the <~,w2,~> key
            TrigramN3C1C2 w2 = new TrigramN3C1C2("~", trigram.getW2(), "~");
            // count the <w1,w2,w3>, <w1,w2> and <w2> appearances
            context.write(trigram, trigramAmount);
            context.write(w1w2, trigramAmount);
            context.write(w2, trigramAmount);
        }
    }

    public static class CombinerClass extends Reducer<TrigramN3C1C2,LongWritable, TrigramN3C1C2,LongWritable> {
        @Override
        public void reduce(TrigramN3C1C2 trigram, Iterable<LongWritable> counts, Context context) throws IOException,  InterruptedException {
            // sum all counts we receive for the Trigrams.TrigramN3C2 in our local device
            long countSum = 0;
            for (LongWritable count : counts) {
                countSum += count.get();
            }
            context.write(trigram, new LongWritable(countSum));
        }
    }

    public static class ReducerClass extends Reducer<TrigramN3C1C2,LongWritable, TrigramN3C1C2, ProbabilityParameters> {
        private LongWritable C2 = new LongWritable(0);
        private LongWritable C1 = new LongWritable(0);

        @Override
        public void reduce(TrigramN3C1C2 trigram, Iterable<LongWritable> counts, Context context) throws IOException, InterruptedException {
            // sum all counts we receive for the Trigrams.TrigramN3C2
            long countSum = 0;
            for (LongWritable count : counts) {
                countSum += count.get();
            }
            // check if this is <w1,w2,~>, <~, w2, ~> or <w1,w2,w3>
            // if <w1,w2,~>, we will update C2 for the next <w1,w2,w3>
            // if <~,w2,~>, we will update C1 for the next <w1,w2,w3>
            // else, we emit the saved C2,C1 and the counterSum (N3)
            if (trigram.getW3().equals("~") && !trigram.getW1().equals("~")) {   // <w1,w2,~>
                this.C2.set(countSum);
            }
            else if(trigram.getW3().equals("~") && trigram.getW1().equals("~")) {   // <~,w2,~>
                this.C1.set(countSum);
            }
            else {
                ProbabilityParameters probabilityParameters = new ProbabilityParameters();
                probabilityParameters.setC2(this.C2);
                probabilityParameters.setN3(new LongWritable(countSum));
                probabilityParameters.setC1(this.C1);
                context.write(trigram, probabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramN3C1C2, LongWritable> {
        @Override
        public int getPartition(TrigramN3C1C2 trigram, LongWritable count, int numPartitions) {
            return (trigram.getW2().hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath, String withCombiner) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "N3C1C2 Counter");
        job.setJarByClass(N3C1C2Counter.class);

        job.setMapperClass(MapperClass.class);
        job.setPartitionerClass(PartitionerClass.class);
        if(withCombiner.equals("true"))
            job.setCombinerClass(CombinerClass.class);
        job.setReducerClass(ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN3C1C2.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(TrigramN3C1C2.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(SequenceFileInputFormat.class);

        System.out.println("Finished configure N3C1C2 job, start executing!");
        job.waitForCompletion(true);
    }
}
