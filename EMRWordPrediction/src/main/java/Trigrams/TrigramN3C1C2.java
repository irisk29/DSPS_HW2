package Trigrams;


public class TrigramN3C1C2 extends AbstractTrigram {
    public TrigramN3C1C2() {
        super();
    }

    public TrigramN3C1C2(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }

    @Override
    public int compareTo(AbstractTrigram other) {
        // compare w2 with other.w2
        // if not equal, return the compareTo value, else continue to next word
        int compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        // we define that * is smaller than everything for <w1,w2,*> to come before <w1,w2,w3>
        if (this.w1.equals("*") && !other.getW1().equals("*"))
            return -1;
        if (other.getW1().equals("*") && !this.w1.equals("*"))
            return 1;
        compareRes = this.w1.compareTo(other.getW1());
        if (compareRes != 0)
            return compareRes;
        // we define that * is smaller than everything for <*,w2,*> to come before <w1,w2,*>
        if (this.w3.equals("*") && !other.getW3().equals("*"))
            return -1;
        if (other.getW3().equals("*") && !this.w3.equals("*"))
            return 1;
        // if not *, return the compareTo value by the last word
        return this.w3.compareTo(other.getW3());
    }
}
