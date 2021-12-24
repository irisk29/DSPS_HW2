import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;

import java.util.ArrayList;
import java.util.List;

public class TrigramWordPrediction {
    public static void main(String[] argv) {
        AWSCredentialsProvider profile;
        try {
            profile = new ProfileCredentialsProvider("default"); // specifies any named profile in .aws/credentials as the credentials provider
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load credentials from .aws/credentials file. " +
                            "Make sure that the credentials file exists and that the profile name is defined within it.",
                    e);
        }

        // create an EMR client using the credentials and region specified in order to create the cluster
        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                .withCredentials(profile)
                .withRegion(Regions.US_EAST_1)
                .build();

        // create a step to enable debugging in the AWS Management Console
        StepFactory stepFactory = new StepFactory();
        StepConfig enabledebugging = new StepConfig()
                .withName("Enable debugging")
                .withActionOnFailure("TERMINATE_JOB_FLOW")
                .withHadoopJarStep(stepFactory.newEnableDebuggingStep());

        // creating map reduces steps for calculating the probabilities
        String jarUrl = "s3://trigramwordprediction/N3C2MapReduce.jar", mainClass = "N3C2Counter";
//        String inputPath = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/3gram/data";
        String inputPath = "s3://trigramwordprediction/ourinput.txt";
        String outputPath = "s3://trigramwordprediction/output.txt";
        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar(jarUrl) // This should be a full map reduce application.
                .withMainClass(mainClass)
                .withArgs(inputPath, outputPath);
        StepConfig n3c2counterStep = new StepConfig()
                .withName("Counter N3 And C2")
                .withActionOnFailure("TERMINATE_JOB_FLOW")
                .withHadoopJarStep(hadoopJarStep);

        List<StepConfig> steps = new ArrayList<>();
        steps.add(enabledebugging);
        steps.add(n3c2counterStep);

        // specify applications to be installed and configured when EMR creates the cluster
        Application hive = new Application().withName("Hive");
        Application spark = new Application().withName("Spark");
        Application ganglia = new Application().withName("Ganglia");
        Application zeppelin = new Application().withName("Zeppelin");

        // create the cluster
        RunJobFlowRequest request = new RunJobFlowRequest()
                .withName("MyClusterCreatedFromJava")
                .withReleaseLabel("emr-6.4.0") // specifies the EMR release version label, we recommend the latest release
                .withSteps(steps)
                .withApplications(hive,spark,ganglia,zeppelin)
                .withLogUri("s3://trigramwordpredictionlogs") // a URI in S3 for log files is required when debugging is enabled
                .withServiceRole("EMR_DefaultRole") // replace the default with a custom IAM service role if one is used
                .withJobFlowRole("EMR_EC2_DefaultRole") // replace the default with a custom EMR role for the EC2 instance profile if one is used
                .withInstances(new JobFlowInstancesConfig()
                        .withEc2KeyName("dsps")
                        .withInstanceCount(3)
                        .withKeepJobFlowAliveWhenNoSteps(true)
                        .withMasterInstanceType("m4.large")
                        .withSlaveInstanceType("m4.large"));

        RunJobFlowResult result = emr.runJobFlow(request);
        System.out.println("The cluster ID is " + result.toString());
    }
}
