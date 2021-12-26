package Trigrams;

import java.util.Objects;

public class TrigramN1C0C1 extends AbstractTrigram {
    public TrigramN1C0C1(String w1, String w2, String w3) {
        super(w1, w2, w3);
    }
    public TrigramN1C0C1() {
        super();
    }
    @Override
    public int compareTo(AbstractTrigram other) {
        // in this compareTo the options we get are:
        // <w1,*,*>, <*,w2,*> <*,*,w3> <w1,w2,w3>
        //we want that all the <w1,w2,w3> trigrams will be last and the singletons first but their order doesn't really matter
        if (this.w3.equals("*") && !other.getW3().equals("*"))
            return -1;
        if (other.getW3().equals("*") && !this.w3.equals("*"))
            return 1;
        if (this.w2.equals("*") && !other.getW2().equals("*"))
            return -1;
        if (other.getW2().equals("*") && !this.w2.equals("*"))
            return 1;
        if (this.w1.equals("*") && !other.getW1().equals("*"))
            return -1;
        if (other.getW1().equals("*") && !this.w1.equals("*"))
            return 1;
        return this.w1.compareTo(other.getW1());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() !=  TrigramN1C0C1.class)
            return false;
        TrigramN1C0C1 other = (TrigramN1C0C1) obj;
        return other.getW1().equals(this.w1) && other.getW2().equals(this.w2) && other.getW3().equals(this.w3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(w1,w2,w3);
    }
}
