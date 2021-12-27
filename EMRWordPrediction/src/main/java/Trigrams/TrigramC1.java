package Trigrams;

public class TrigramC1 extends AbstractTrigram{

    public TrigramC1() {
        super();
    }

    public TrigramC1(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        int compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        if (this.w3.equals("*") && !other.getW3().equals("*"))
            return -1;
        if (other.getW3().equals("*") && !this.w3.equals("*"))
            return 1;
        if (this.w1.equals("*") && !other.getW1().equals("*"))
            return -1;
        if (other.getW1().equals("*") && !this.w1.equals("*"))
            return 1;
        // if not *, return the compareTo value by the last word
        return this.w1.compareTo(other.getW1());
    }
}
