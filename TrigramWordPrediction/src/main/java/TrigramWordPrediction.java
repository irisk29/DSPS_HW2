import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class TrigramWordPrediction {
    static String jarUrl;
    static String outputPath;
    static String logPath;
    static String mainClass;
    static String keyName;
    static String withCombiner;
    static int numOfInstances;

    public static void readInformationFromJson()
    {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObj = (JSONObject) parser.parse(new FileReader("info.json"));
            jarUrl = jsonObj.get("jar-path").toString();
            outputPath = jsonObj.get("output-path").toString();
            logPath = jsonObj.get("log-path").toString();
            mainClass = jsonObj.get("main-class").toString();
            keyName = jsonObj.get("key-name").toString();
            numOfInstances = Integer.parseInt(jsonObj.get("num-of-instances").toString());
            withCombiner = jsonObj.get("with-combiner").toString();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

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
        readInformationFromJson();
        // create an EMR client using the credentials and region specified in order to create the cluster
        AmazonElasticMapReduce emr = AmazonElasticMapReduceClientBuilder.standard()
                .withCredentials(profile)
                .withRegion(Regions.US_EAST_1)
                .build();

        // creating map reduces steps for calculating the probabilities
        String inputPath = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/3gram/data";
        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar(jarUrl) // This should be a full map reduce application.
                .withMainClass(mainClass)
                .withArgs(inputPath, outputPath, withCombiner);
        StepConfig emrWordPrediction = new StepConfig()
                .withName("EMR Word Predication")
                .withActionOnFailure("TERMINATE_JOB_FLOW")
                .withHadoopJarStep(hadoopJarStep);

        // create the cluster
        RunJobFlowRequest request = new RunJobFlowRequest()
                .withName("MyClusterCreatedFromJava")
                .withReleaseLabel("emr-6.4.0") // specifies the EMR release version label, we recommend the latest release
                .withSteps(emrWordPrediction)
                .withLogUri(logPath) // a URI in S3 for log files is required when debugging is enabled
                .withServiceRole("EMR_DefaultRole") // replace the default with a custom IAM service role if one is used
                .withJobFlowRole("EMR_EC2_DefaultRole") // replace the default with a custom EMR role for the EC2 instance profile if one is used
                .withInstances(new JobFlowInstancesConfig()
                        .withEc2KeyName(keyName)
                        .withInstanceCount(numOfInstances)
                        .withKeepJobFlowAliveWhenNoSteps(false)
                        .withMasterInstanceType(InstanceType.M5Xlarge.toString())
                        .withSlaveInstanceType(InstanceType.M5Xlarge.toString()));

        RunJobFlowResult result = emr.runJobFlow(request);
        System.out.println("The cluster ID is " + result.toString());
    }
}
