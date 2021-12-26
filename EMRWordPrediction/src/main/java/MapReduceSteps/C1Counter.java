package MapReduceSteps;

import InputFormats.N1C0C1InputFormat;
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

public class C1Counter {
    public static class MapperClass extends Mapper<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private final static IntWritable one = new IntWritable(1);

        @Override
        public void map(TrigramN1C0C1 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramN1C0C1 w2 = new TrigramN1C0C1("*", trigram.getW2(), "*");
            // save in counter the appearance of this words
            probabilityParameters.setC1(one);
            // count the <w1,w2,w3> <*,*,w3> appearances
            context.write(w2, probabilityParameters);
            context.write(trigram, probabilityParameters);

        }
    }

    public static class CombinerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {

            ProbabilityParameters updatedProbabilityParameters = null;
            if(trigram.getW1().equals("*") && !trigram.getW2().equals("*")) //<*,w2,*>
            {
                int c1CounterSum = 0;
                for (ProbabilityParameters probabilityParameters : counts) {
                    updatedProbabilityParameters = probabilityParameters;
                    c1CounterSum += probabilityParameters.getC1().get();
                }
                assert updatedProbabilityParameters != null;
                updatedProbabilityParameters.setC1(new IntWritable(c1CounterSum));
                context.write(trigram, updatedProbabilityParameters);
            }
            else
            {
                context.write(trigram, counts.iterator().next());
            }
        }
    }

    public static class ReducerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private int C1 = 0;

        @Override
        public void setup(Context context){}

        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
            if(!trigram.getW2().equals("*") && trigram.getW1().equals("*")) //<*,w2,*>
            {
                for (ProbabilityParameters probabilityParameters : counts) {
                    C1 += probabilityParameters.getC1().get();
                }
            }
            else //<w1,w2,w3>
            {
                ProbabilityParameters probabilityParameters = counts.iterator().next();
                probabilityParameters.setC1(new IntWritable(C1));
                context.write(trigram, probabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramN1C0C1 trigram, ProbabilityParameters count, int numPartitions) {
            return trigram.getW2().hashCode() % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "C1 Counter");
        job.setJarByClass(C1Counter.class);

        job.setMapperClass(C1Counter.MapperClass.class);
        job.setPartitionerClass(C1Counter.PartitionerClass.class);
        job.setCombinerClass(C1Counter.CombinerClass.class);
        job.setReducerClass(C1Counter.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN1C0C1.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramN1C0C1.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N1C0C1InputFormat.class);

        System.out.println("Finished configure C1 job, start executing!");
        job.waitForCompletion(true);
    }
}
