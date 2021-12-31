package MapReduceSteps;

import InputFormats.C0N1N2TrigramInputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramC0N1N2;
import Trigrams.TrigramResult;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class SortResult {
    public static class MapperClass extends Mapper<TrigramC0N1N2, ProbabilityParameters, TrigramResult, DoubleWritable> {

        @Override
        public void map(TrigramC0N1N2 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            double prob = probabilityParameters.calcProb();
            TrigramResult resTri = new TrigramResult(trigram.getW1(), trigram.getW2(), trigram.getW3(), prob);
            context.write(resTri, new DoubleWritable(prob));
        }
    }

    public static class ReducerClass extends Reducer<TrigramResult, DoubleWritable, TrigramResult, DoubleWritable> {

        @Override
        public void reduce(TrigramResult trigram, Iterable<DoubleWritable> counts, Context context) throws IOException, InterruptedException {
            context.write(trigram, counts.iterator().next());
        }
    }

    public static class PartitionerClass extends Partitioner<TrigramResult, DoubleWritable> {
        @Override
        public int getPartition(TrigramResult trigram, DoubleWritable count, int numPartitions) {
            // we want all trigrams that start with the same w2,w3 go to the same reducer
            return ((trigram.getW1().hashCode() + trigram.getW2().hashCode()) & Integer.MAX_VALUE) % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Sort Job");
        job.setJarByClass(SortResult.class);

        job.setMapperClass(SortResult.MapperClass.class);
        job.setPartitionerClass(SortResult.PartitionerClass.class);
        job.setReducerClass(SortResult.ReducerClass.class);

        job.setMapOutputKeyClass(TrigramResult.class);
        job.setMapOutputValueClass(DoubleWritable.class);
        job.setOutputKeyClass(TrigramResult.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(C0N1N2TrigramInputFormat.class);

        System.out.println("Finished configure Sort job, start executing!");
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
