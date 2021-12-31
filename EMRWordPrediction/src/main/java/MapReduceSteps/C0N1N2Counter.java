package MapReduceSteps;

import InputFormats.N3C1C2TrigramInputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramC0N1N2;
import Trigrams.TrigramN3C1C2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class C0N1N2Counter {
    static enum c0Counter { TOTAL_WORDS }
    public static class MapperClass extends Mapper<TrigramN3C1C2, ProbabilityParameters, TrigramC0N1N2, ProbabilityParameters> {
        private final static LongWritable one = new LongWritable(1);

        @Override
        public void map(TrigramN3C1C2 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramC0N1N2 trigramN1N2 = new TrigramC0N1N2(trigram.getW1(), trigram.getW2(), trigram.getW3());
            System.out.println("got new trigram in mapper: " + trigramN1N2);
            // create the <~,w2,w3> key
            TrigramC0N1N2 w2w3 = new TrigramC0N1N2("~", trigram.getW2(), trigram.getW3());
            TrigramC0N1N2 w3 = new TrigramC0N1N2("~", "~", trigram.getW3());
            // save in counter the appearance of this words
            probabilityParameters.setN2(one);
            probabilityParameters.setN1(one);
            //count the number of words in the corpus
            context.getCounter(C0N1N2Counter.c0Counter.TOTAL_WORDS).increment(probabilityParameters.getN3().get() * 3);
            // count the <w1,w2,w3> and <w2,w3> <w3> appearances
            context.write(trigramN1N2, probabilityParameters);
            context.write(w2w3, probabilityParameters);
            context.write(w3, probabilityParameters);
        }
    }

    public static class CombinerClass extends Reducer<TrigramC0N1N2, ProbabilityParameters, TrigramC0N1N2, ProbabilityParameters> {
        @Override
        public void reduce(TrigramC0N1N2 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {
            // sum all counts we receive for the trigram in our local device
            long n2CountSum = 0, n1CountSSum = 0;
            ProbabilityParameters updatedProbabilityParameters = null;
            for (ProbabilityParameters probabilityParameters : counts) {
                updatedProbabilityParameters = probabilityParameters;
                n2CountSum += probabilityParameters.getN2().get();
                n1CountSSum += probabilityParameters.getN1().get();
            }
            assert updatedProbabilityParameters != null;
            updatedProbabilityParameters.setN2(new LongWritable(n2CountSum));
            updatedProbabilityParameters.setN1(new LongWritable(n1CountSSum));
            context.write(trigram, updatedProbabilityParameters);
            System.out.println("finish combining local trigram: " + trigram.toString());
        }
    }

    public static class ReducerClass extends Reducer<TrigramC0N1N2, ProbabilityParameters, TrigramC0N1N2, ProbabilityParameters> {
        private LongWritable N2 = new LongWritable(0);
        private LongWritable N1 = new LongWritable(0);
        private LongWritable C0 = new LongWritable(0);

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            Cluster cluster = new Cluster(conf);
            Job currentJob = cluster.getJob(context.getJobID());
            C0.set(currentJob.getCounters().findCounter(C0N1N2Counter.c0Counter.TOTAL_WORDS).getValue());
            System.out.println(C0);
        }

        @Override
        public void reduce(TrigramC0N1N2 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
            // sum all counts we receive for the trigram
            long n2CountSum = 0, n1CountSum = 0;
            ProbabilityParameters updatedProbabilityParameters = null;
            for (ProbabilityParameters probabilityParameters : counts) {
                updatedProbabilityParameters = probabilityParameters;
                n2CountSum += probabilityParameters.getN2().get();
                n1CountSum += probabilityParameters.getN1().get();
            }
            // check if this is <~,w2,w3> or <w1,w2,w3> <~,~,w3>
            // if <~,w2,w3>, we will update N2 for the next <w1,w2,w3>
            // if <~,~,w3> we will update N1 for next <w1,w2,w3>
            // else, we emit the saved N2 and the updatedProbabilityParameters
            if (trigram.getW1().equals("~") && !trigram.getW2().equals("~")) { // <~,w2,w3>
                this.N2.set(n2CountSum);
            }
            else if(trigram.getW1().equals("~") && trigram.getW2().equals("~")) // <~,~,w3>
            {
                this.N1.set(n1CountSum);
            }
            else {
                System.out.println("reducer got new trigram <w1,w2,w3>: " + trigram);
                assert updatedProbabilityParameters != null;
                updatedProbabilityParameters.setN2(N2);
                updatedProbabilityParameters.setN1(N1);
                updatedProbabilityParameters.setC0(C0);
                context.write(trigram, updatedProbabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramC0N1N2, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramC0N1N2 trigram, ProbabilityParameters count, int numPartitions) {
            // we want all trigrams that start with the same w2,w3 go to the same reducer
            return (trigram.getW3().hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath, String withCombiner) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "C0N1N2 Counter");
        job.setJarByClass(C0N1N2Counter.class);

        job.setMapperClass(C0N1N2Counter.MapperClass.class);
        job.setPartitionerClass(C0N1N2Counter.PartitionerClass.class);
        if(withCombiner.equals("true"))
            job.setCombinerClass(C0N1N2Counter.CombinerClass.class);
        job.setReducerClass(C0N1N2Counter.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramC0N1N2.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramC0N1N2.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N3C1C2TrigramInputFormat.class);

        System.out.println("Finished configure C0N1N2 job, start executing!");
        job.waitForCompletion(true);
    }
}
