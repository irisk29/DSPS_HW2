import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;


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

        // creating map reduces steps for calculating the probabilities
        String jarUrl = "s3://trigramwordprediction2/EMRWordPrediction.jar";
        String inputPath = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/3gram/data";
        //String inputPath = "s3://trigramwordprediction2/ourinput.txt";
        String outputPath = "s3://trigramwordprediction2";
        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar(jarUrl) // This should be a full map reduce application.
                .withMainClass("Main")
                .withArgs(inputPath, outputPath);
        StepConfig emrWordPrediction = new StepConfig()
                .withName("EMR Word Predication")
                .withActionOnFailure("TERMINATE_JOB_FLOW")
                .withHadoopJarStep(hadoopJarStep);

        // specify applications to be installed and configured when EMR creates the cluster
        Application hive = new Application().withName("Hive");
        Application spark = new Application().withName("Spark");
        Application ganglia = new Application().withName("Ganglia");
        Application zeppelin = new Application().withName("Zeppelin");

        // create the cluster
        RunJobFlowRequest request = new RunJobFlowRequest()
                .withName("MyClusterCreatedFromJava")
                .withReleaseLabel("emr-6.4.0") // specifies the EMR release version label, we recommend the latest release
                .withSteps(emrWordPrediction)
                .withApplications(hive,spark,ganglia,zeppelin)
                .withLogUri("s3://trigramwordpredictionlogs2") // a URI in S3 for log files is required when debugging is enabled
                .withServiceRole("EMR_DefaultRole") // replace the default with a custom IAM service role if one is used
                .withJobFlowRole("EMR_EC2_DefaultRole") // replace the default with a custom EMR role for the EC2 instance profile if one is used
                .withInstances(new JobFlowInstancesConfig()
                        .withEc2KeyName("dsps")
                        .withInstanceCount(3)
                        .withKeepJobFlowAliveWhenNoSteps(false)
                        .withMasterInstanceType("m4.large")
                        .withSlaveInstanceType("m4.large"));

        RunJobFlowResult result = emr.runJobFlow(request);
        System.out.println("The cluster ID is " + result.toString());
    }
}
