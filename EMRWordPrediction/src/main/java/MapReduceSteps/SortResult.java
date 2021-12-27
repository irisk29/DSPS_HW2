package MapReduceSteps;

import InputFormats.N1C0C1InputFormat;
import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN1C0C1;
import Trigrams.TrigramResult;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class SortResult {
    public static class MapperClass extends Mapper<TrigramN1C0C1, ProbabilityParameters, TrigramResult, DoubleWritable> {

        @Override
        public void map(TrigramN1C0C1 trigram, ProbabilityParameters probabilityParameters, Context context) throws IOException,  InterruptedException {
            TrigramResult resTri = new TrigramResult(trigram.getW1(), trigram.getW2(), trigram.getW3());
            long n1 = probabilityParameters.getN1().get();
            long n2 = probabilityParameters.getN2().get();
            long n3 = probabilityParameters.getN3().get();
            long c0 = probabilityParameters.getC0().get();
            long c1 = probabilityParameters.getC1().get();
            long c2 = probabilityParameters.getC2().get();

            double k3 = (Math.log(n3 + 1) + 1) / (Math.log(n3 + 1) + 2);
            double k2 = (Math.log(n2 + 1) + 1) / (Math.log(n2 + 1) + 2);

            double prob = k3 * (((double)n3)/ c2) + (1 - k3) * k2 * (((double)n2) / c1) + (1 - k3) * (1 - k2) * (((double)n1) / c0);
            resTri.setProb(prob);
            context.write(resTri, new DoubleWritable(prob));
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Sort Job");
        job.setJarByClass(C0Counter.class);

        job.setMapperClass(SortResult.MapperClass.class);
        job.setNumReduceTasks(0);

        job.setMapOutputKeyClass(TrigramResult.class);
        job.setMapOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(N1C0C1InputFormat.class);

        System.out.println("Finished configure Sort job, start executing!");
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
