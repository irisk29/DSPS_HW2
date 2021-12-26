package MapReduceSteps;

import InputFormats.N2TrigramInputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN1C0C1;
import Trigrams.TrigramN2;
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
import java.util.HashMap;

public class N1Counter {
    public static class MapperClass extends Mapper<TrigramN2, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private final static IntWritable one = new IntWritable(1);

        @Override
        public void map(TrigramN2 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramN1C0C1 newTrigram = new TrigramN1C0C1(trigram.getW1(), trigram.getW2(), trigram.getW3());
            System.out.println("got new trigram in mapper: " + newTrigram);

            TrigramN1C0C1 w3 = new TrigramN1C0C1("*", "*", trigram.getW3());
            // save in counter the appearance of this words
            probabilityParameters.setN1(one);
            // count the <w1,w2,w3> <*,*,w3> appearances
            context.write(w3, probabilityParameters);
            context.write(newTrigram, probabilityParameters);

        }
    }

    public static class CombinerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {

            ProbabilityParameters updatedProbabilityParameters = null;
            if(trigram.getW1().equals("*") && !trigram.getW3().equals("*")) //<*,*,w3>
            {
                int n1CounterSum = 0;
                for (ProbabilityParameters probabilityParameters : counts) {
                    updatedProbabilityParameters = probabilityParameters;
                    n1CounterSum += probabilityParameters.getN1().get();
                }
                assert updatedProbabilityParameters != null;
                updatedProbabilityParameters.setN1(new IntWritable(n1CounterSum));
                context.write(trigram, updatedProbabilityParameters);
            }
            else
            {
                context.write(trigram, counts.iterator().next());
            }
        }
    }

    public static class ReducerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private int N1 = 0;
        @Override
        public void setup(Context context){}

        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
            if(!trigram.getW3().equals("*") && trigram.getW2().equals("*")) //<*,*,w3>
            {
                for (ProbabilityParameters probabilityParameters : counts) {
                    N1 += probabilityParameters.getN1().get();
                }
            }
            else //<w1,w2,w3>
            {
                ProbabilityParameters probabilityParameters = counts.iterator().next();
                probabilityParameters.setN1(new IntWritable(N1));
                context.write(trigram, probabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramN1C0C1 trigram, ProbabilityParameters count, int numPartitions) {
            return trigram.getW3().hashCode() % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "N1 Counter");
        job.setJarByClass(N1Counter.class);

        job.setMapperClass(N1Counter.MapperClass.class);
        job.setPartitionerClass(N1Counter.PartitionerClass.class);
        job.setCombinerClass(N1Counter.CombinerClass.class);
        job.setReducerClass(N1Counter.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN1C0C1.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramN1C0C1.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N2TrigramInputFormat.class);

        System.out.println("Finished configure N1 job, start executing!");
        job.waitForCompletion(true);
    }
}
