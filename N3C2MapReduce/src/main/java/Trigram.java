import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Trigram implements WritableComparable<Trigram> {
    private String w1;
    private String w2;
    private String w3;

    public Trigram() {
        this.w1 = "?";
        this.w2 = "?";
        this.w3 = "?";
    }

    public Trigram(String w1, String w2, String w3) {
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
    }

    public String getW1() {
        return w1;
    }

    public void setW1(String w1) {
        this.w1 = w1;
    }

    public String getW2() {
        return w2;
    }

    public void setW2(String w2) {
        this.w2 = w2;
    }

    public String getW3() {
        return w3;
    }

    public void setW3(String w3) {
        this.w3 = w3;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.w1 = in.readUTF();
        this.w2 = in.readUTF();
        this.w3 = in.readUTF();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(this.w1);
        out.writeUTF(this.w2);
        out.writeUTF(this.w3);
    }

    @Override
    public int compareTo(Trigram other) {
        // compare w1 with other.w1
        // if not equal, return the compareTo value, else continue to next word
        int compareRes = this.w1.compareTo(other.getW1());
        if (compareRes != 0)
            return compareRes;
        // compare w2 with other.w2
        // if not equal, return the compareTo value, else continue to next word
        compareRes = this.w2.compareTo(other.getW2());
        if (compareRes != 0)
            return compareRes;
        // we define that * is smaller than everything for <w1,w2,*> to come before <w1,w2,w3>
        if (this.w3.equals("*"))
            return -1;
        // if not *, return the compareTo value by the last word
        return this.w3.compareTo(other.getW3());
    }

    @Override
    public String toString() {
        return this.w1 + "," + this.w2 + "," + this.w3;
    }
}
