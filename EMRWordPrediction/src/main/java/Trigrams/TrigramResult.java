package Trigrams;

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

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = prob;
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
        return Double.compare(this.prob, otherRes.prob);
    }

}
