package Trigrams;


public class TrigramN2 extends AbstractTrigram {
    public TrigramN2() {
        super();
    }

    public TrigramN2(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        // compare w2 with other.w2
        // if not equal, return the compareTo value, else continue to next word
        int compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        // compare w3 with other.w3
        // if not equal, return the compareTo value, else continue to next word
        compareRes = this.w3.compareTo(other.getW3());
        if (compareRes != 0)
            return compareRes;
        // we define that * is smaller than everything for <*,w2,w3> to come before <w1,w2,w3>
        if (this.w1.equals("*") && !other.getW1().equals("*"))
            return -1;
        if (other.getW1().equals("*") && !this.w1.equals("*"))
            return 1;
        // if not *, return the compareTo value by the last word
        return this.w1.compareTo(other.getW1());
    }
}