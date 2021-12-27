import MapReduceSteps.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputStep1 = args[1], outputPath = args[2];

        N3C2Counter.runMain(inputStep1, outputPath + "/outputStep1");
        N2Counter.runMain(outputPath + "/outputStep1", outputPath + "/outputStep2");
        N1Counter.runMain(outputPath + "/outputStep2", outputPath + "/outputStep3");
        C1Counter.runMain(outputPath + "/outputStep3", outputPath + "/outputStep4");
        C0Counter.runMain(outputPath + "/outputStep4", outputPath + "/outputStep5");
        SortResult.runMain(outputPath + "/outputStep5", outputPath + "/outputStep6");
    }
}
