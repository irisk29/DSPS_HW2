package Trigrams;

public class TrigramResult extends AbstractTrigram{
    private double prob;

    public TrigramResult(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }
    public TrigramResult() {
        super();
    }

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = prob;
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        // in this compareTo the options we get are:
        // <w1,*,*>, <*,w2,*> <*,*,w3> <w1,w2,w3>
        //we want that all the <w1,w2,w3> trigrams will be last and the singletons first but their order doesn't really matter
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
