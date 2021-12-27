package Trigrams;

public class TrigramC0 extends AbstractTrigram{
    public TrigramC0() {
        super();
    }

    public TrigramC0(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        if(this.w1.equals("*") && !other.w1.equals("*"))
            return -1;
        if(this.w1.equals("*") && other.w1.equals("*"))
            return 0;
        int compareRes = this.w1.compareTo(other.getW1());
        if (compareRes != 0)
            return compareRes;
        compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        return this.w3.compareTo(other.getW3());
    }
}
