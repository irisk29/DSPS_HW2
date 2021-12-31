import MapReduceSteps.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputStep1 = args[1], outputPath = args[2], withCombiner = args[3];

        N3C1C2Counter.runMain(inputStep1, outputPath + "/outputStep1", withCombiner);
        C0N1N2Counter.runMain(outputPath + "/outputStep1", outputPath + "/outputStep2", withCombiner);
        CalculateProbability.runMain(outputPath + "/outputStep2", outputPath + "/outputStep3");
    }
}
