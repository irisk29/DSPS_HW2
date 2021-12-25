package MapReduceSteps;

import InputFormats.N3C2TrigramInputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN2;
import Trigrams.TrigramN3C2;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class N2Counter {
    public static class MapperClass extends Mapper<TrigramN3C2, ProbabilityParameters, TrigramN2, ProbabilityParameters> {
        private final static IntWritable one = new IntWritable(1);

        @Override
        public void map(TrigramN3C2 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramN2 trigramN2 = new TrigramN2(trigram.getW1(), trigram.getW2(), trigram.getW3());
            System.out.println("got new trigram in mapper: " + trigramN2);
            // create the <*,w2,w3> key
            TrigramN2 w2w3 = new TrigramN2("*", trigram.getW2(), trigram.getW3());
            // save in counter the appearance of this words
            probabilityParameters.setN2(one);
            // count the <w1,w2,w3> and <w1,w2> appearances
            context.write(trigramN2, probabilityParameters);
            context.write(w2w3, probabilityParameters);
        }
    }

    public static class CombinerClass extends Reducer<TrigramN2, ProbabilityParameters, TrigramN2, ProbabilityParameters> {
        @Override
        public void reduce(TrigramN2 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {
            // sum all counts we receive for the trigram in our local device
            int n2CountSum = 0;
            ProbabilityParameters updatedProbabilityParameters = null;
            for (ProbabilityParameters probabilityParameters : counts) {
                updatedProbabilityParameters = probabilityParameters;
                n2CountSum += probabilityParameters.getN2().get();
            }
            assert updatedProbabilityParameters != null;
            updatedProbabilityParameters.setN2(new IntWritable(n2CountSum));
            context.write(trigram, updatedProbabilityParameters);
            System.out.println("finish combining local trigram: " + trigram.toString());
        }
    }

    public static class ReducerClass extends Reducer<TrigramN2, ProbabilityParameters, TrigramN2, ProbabilityParameters> {
        private IntWritable N2 = new IntWritable();

        @Override
        public void setup(Context context) {
            this.N2.set(0);
        }

        @Override
        public void reduce(TrigramN2 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
            // sum all counts we receive for the trigram
            int n2CountSum = 0;
            ProbabilityParameters updatedProbabilityParameters = null;
            for (ProbabilityParameters probabilityParameters : counts) {
                updatedProbabilityParameters = probabilityParameters;
                n2CountSum += probabilityParameters.getN2().get();
            }
            // check if this is <*,w2,w3> or <w1,w2,w3>
            // if <*,w2,w3>, we will update N2 for the next <w1,w2,w3>
            // else, we emit the saved N2 and the updatedProbabilityParameters
            if (trigram.getW1().equals("*")) {
                this.N2.set(n2CountSum);
            }
            else {
                System.out.println("reducer got new trigram <w1,w2,w3>: " + trigram);
                assert updatedProbabilityParameters != null;
                updatedProbabilityParameters.setN2(new IntWritable(n2CountSum));
                context.write(trigram, updatedProbabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramN2, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramN2 trigram, ProbabilityParameters count, int numPartitions) {
            // we want all trigrams that start with the same w2,w3 go to the same reducer
            return (trigram.getW2().hashCode() + trigram.getW3().hashCode()) % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "N2 Counter");
        job.setJarByClass(N2Counter.class);

        job.setMapperClass(N2Counter.MapperClass.class);
        job.setPartitionerClass(N2Counter.PartitionerClass.class);
        job.setCombinerClass(N2Counter.CombinerClass.class);
        job.setReducerClass(N2Counter.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN2.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramN2.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N3C2TrigramInputFormat.class);

        System.out.println("Finished configure N2 job, start executing!");
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
