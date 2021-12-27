package MapReduceSteps;

import InputFormats.C1TrigramInputFormat;
import InputFormats.N1C0C1InputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramC0;
import Trigrams.TrigramC1;
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
    public static class MapperClass extends Mapper<TrigramC1, ProbabilityParameters, TrigramC0, ProbabilityParameters> {
        private final static LongWritable one = new LongWritable(1);

        @Override
        public void map(TrigramC1 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramC0 anyWord = new TrigramC0("*", "*", "*");
            TrigramC0 trigramC0 = new TrigramC0(trigram.getW1(), trigram.getW2(), trigram.getW3());
            // save in counter the appearance of this words
            probabilityParameters.setC0(one);
            // count the <w1,w2,w3> <*,*,*> appearances
            context.write(anyWord, probabilityParameters);
            context.write(trigramC0, probabilityParameters);

        }
    }

    public static class CombinerClass extends Reducer<TrigramC0, ProbabilityParameters, TrigramC0, ProbabilityParameters> {
        @Override
        public void reduce(TrigramC0 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException,  InterruptedException {

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

    public static class ReducerClass extends Reducer<TrigramC0, ProbabilityParameters, TrigramC0, ProbabilityParameters> {
        private long C0 = 0;

        @Override
        public void setup(Context context){}

        @Override
        public void reduce(TrigramC0 trigram, Iterable<ProbabilityParameters> counts, Context context) throws IOException, InterruptedException {
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

    public static class PartitionerClass extends Partitioner<TrigramC0, ProbabilityParameters> {
        @Override
        public int getPartition(TrigramC0 trigram, ProbabilityParameters count, int numPartitions) {
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

        job.setMapOutputKeyClass(TrigramC0.class);
        job.setMapOutputValueClass(ProbabilityParameters.class);
        job.setOutputKeyClass(TrigramC0.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(C1TrigramInputFormat.class);

        System.out.println("Finished configure C0 job, start executing!");
        job.waitForCompletion(true);
    }
}
