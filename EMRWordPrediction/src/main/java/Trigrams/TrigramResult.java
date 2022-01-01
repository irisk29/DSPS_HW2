package Trigrams;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TrigramResult extends AbstractTrigram{
    private double prob;

    public TrigramResult(String w1, String w2, String w3, double prob) {
        super(w1, w2, w3);
        this.prob = prob;
    }
    public TrigramResult() {
        super();
        prob = 0.0;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.readFields(in);
        this.prob = in.readDouble();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        super.write(out);
        out.writeDouble(this.prob);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        TrigramResult otherRes = (TrigramResult) other;
        int compareRes = this.w1.compareTo(other.getW1());
        if (compareRes != 0)
            return compareRes;
        // compare w2 with other.w2
        // if not equal, return the compareTo value, else continue to next word
        compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        compareRes = Double.compare(this.prob, otherRes.prob);
        if(compareRes != 0)
            return compareRes * (-1);
        return this.w3.compareTo(other.getW3());
    }

}
