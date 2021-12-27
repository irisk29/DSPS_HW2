package MapReduceSteps;

import InputFormats.N1C0C1InputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN1C0C1;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class C0Counter {
    public static class MapperClass extends Mapper<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private final static LongWritable one = new LongWritable(1);

        @Override
        public void map(TrigramN1C0C1 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramN1C0C1 anyWord = new TrigramN1C0C1("*", "*", "*");
            // save in counter the appearance of this words
            probabilityParameters.setC0(one);
            // count the <w1,w2,w3> <*,*,*> appearances
            context.write(anyWord, probabilityParameters);
            context.write(trigram, probabilityParameters);

        }
    }

    public static class CombinerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {

            ProbabilityParameters updatedProbabilityParameters = null;
            if(trigram.getW1().equals("*"))
            {
                int c0CounterSum = 0;
                for (ProbabilityParameters probabilityParameters : counts) {
                    updatedProbabilityParameters = probabilityParameters;
                    c0CounterSum += probabilityParameters.getC0().get();
                }
                assert updatedProbabilityParameters != null;
                updatedProbabilityParameters.setC0(new LongWritable(c0CounterSum));
                context.write(trigram, updatedProbabilityParameters);
            }
            else
            {
                context.write(trigram, counts.iterator().next());
            }
        }
    }

    public static class ReducerClass extends Reducer<TrigramN1C0C1, ProbabilityParameters, TrigramN1C0C1, ProbabilityParameters> {
        private long C0 = 0;

        @Override
        public void setup(Context context){}

        @Override
        public void reduce(TrigramN1C0C1 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
            if(trigram.getW1().equals("*")) //<*,*,*>
            {
                for (ProbabilityParameters probabilityParameters : counts) {
                    C0 += (probabilityParameters.getC0().get() * 3); //there are three words in every trigram
                }
            }
            else //<w1,w2,w3>
            {
                ProbabilityParameters probabilityParameters = counts.iterator().next();
                probabilityParameters.setC0(new LongWritable(C0));
                context.write(trigram, probabilityParameters);
            }
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramN1C0C1, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramN1C0C1 trigram, ProbabilityParameters count, int numPartitions) {
            return 0;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "C0 Counter");
        job.setJarByClass(C0Counter.class);

        job.setMapperClass(C0Counter.MapperClass.class);
        job.setPartitionerClass(C0Counter.PartitionerClass.class);
        job.setCombinerClass(C0Counter.CombinerClass.class);
        job.setReducerClass(C0Counter.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN1C0C1.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramN1C0C1.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N1C0C1InputFormat.class);

        System.out.println("Finished configure C0 job, start executing!");
        job.waitForCompletion(true);
    }
}
