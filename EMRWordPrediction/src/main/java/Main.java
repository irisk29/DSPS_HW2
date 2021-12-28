import MapReduceSteps.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputStep1 = args[1], outputPath = args[2];

        N3C1C2Counter.runMain(inputStep1, outputPath + "/outputStep1");
        C0N1N2Counter.runMain(outputPath + "/outputStep1", outputPath + "/outputStep2");
        SortResult.runMain(outputPath + "/outputStep2", outputPath + "/outputStep3");
    }
}
