import MapReduceSteps.N2Counter;
import MapReduceSteps.N3C2Counter;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputStep1 = args[1], outputPath = args[2];

        N3C2Counter.runMain(inputStep1, outputPath + "/outputStep1");
        N2Counter.runMain(outputPath + "/outputStep1", outputPath + "/outputStep2");
    }
}
