package Trigrams;

public class TrigramN1 extends AbstractTrigram{

    public TrigramN1() {
        super();
    }

    public TrigramN1(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        int compareRes = this.w3.compareTo(other.getW3());
        if (compareRes != 0)
            return compareRes;
        if (this.w2.equals("*") && !other.getW2().equals("*"))
            return -1;
        if (other.getW2().equals("*") && !this.w2.equals("*"))
            return 1;
        if (this.w1.equals("*") && !other.getW1().equals("*"))
            return -1;
        if (other.getW1().equals("*") && !this.w1.equals("*"))
            return 1;
        // if not *, return the compareTo value by the last word
        return this.w1.compareTo(other.getW1());
    }

}
