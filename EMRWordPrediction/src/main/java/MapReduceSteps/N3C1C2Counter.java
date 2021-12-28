package MapReduceSteps;

import ProbabilityParameters.ProbabilityParameters;
import Trigrams.TrigramN3C1C2;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class N3C1C2Counter {

    public static class MapperClass extends Mapper<LongWritable, Text, TrigramN3C1C2, LongWritable> {
        private final static LongWritable one = new LongWritable(1);

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException,  InterruptedException {
            StringTokenizer itrTokenizedValue = new StringTokenizer(value.toString(), "\t");
            // gets the three hebrew words
            String[] TrigramN3C2Words = itrTokenizedValue.nextToken().split(" ");
            TrigramN3C1C2 trigram = new TrigramN3C1C2(TrigramN3C2Words[0], TrigramN3C2Words[1], TrigramN3C2Words[2]);
            System.out.println("got new Trigrams.TrigramN3C2 in mapper: " + trigram);
            // create the <w1,w2,*> key
            TrigramN3C1C2 w1w2 = new TrigramN3C1C2(trigram.getW1(), trigram.getW2(), "*");
            TrigramN3C1C2 w2 = new TrigramN3C1C2("*", trigram.getW2(), "*");
            // count the <w1,w2,w3> and <w1,w2> appearances
            context.write(trigram, one);
            context.write(w1w2, one);
            context.write(w2, one);
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
            System.out.println("finish combining local Trigrams.TrigramN3C2: " + trigram.toString());
        }
    }

    public static class ReducerClass extends Reducer<TrigramN3C1C2,LongWritable, TrigramN3C1C2, ProbabilityParameters> {
        private LongWritable C2 = new LongWritable(0);
        private LongWritable C1 = new LongWritable(0);

        @Override
        public void setup(Context context) {}

        @Override
        public void reduce(TrigramN3C1C2 trigram, Iterable<LongWritable> counts, Context context) throws IOException, InterruptedException {
            // sum all counts we receive for the Trigrams.TrigramN3C2
            long countSum = 0;
            for (LongWritable count : counts) {
                countSum += count.get();
            }
            // check if this is <w1,w2,*> or <w1,w2,w3>
            // if <w1,w2,*>, we will update C2 for the next <w1,w2,w3>
            // if <*,w2,*>, we will update C1 for the next <w1,w2,w3>
            // else, we emit the saved C2,C1 and the counterSum (N3)
            if (trigram.getW3().equals("*") && !trigram.getW1().equals("*")) {
                System.out.println("reducer got new Trigrams.TrigramN3C2 <w1,w2,*>: " + trigram);
                this.C2.set(countSum);
            }
            else if(trigram.getW3().equals("*") && trigram.getW1().equals("*")) //<*,w2,*>
            {
                this.C1.set(countSum);
            }
            else {
                System.out.println("reducer got new Trigrams.TrigramN3C2 <w1,w2,w3>: " + trigram);
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
            return trigram.getW2().hashCode() % numPartitions;
        }
    }

    public static void runMain(String inputPath, String outputPath) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "N3C1C2 Counter");
        job.setJarByClass(N3C1C2Counter.class);

        job.setMapperClass(MapperClass.class);
        job.setPartitionerClass(PartitionerClass.class);
        job.setCombinerClass(CombinerClass.class);
        job.setReducerClass(ReducerClass.class);

        job.setMapOutputKeyClass(TrigramN3C1C2.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(TrigramN3C1C2.class);
        job.setOutputValueClass(ProbabilityParameters.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        System.out.println("Finished configure N3C1C2 job, start executing!");
        job.waitForCompletion(true);
    }
}
