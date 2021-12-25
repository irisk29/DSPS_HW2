package Trigrams;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractTrigram implements WritableComparable<AbstractTrigram> {
    protected String w1;
    protected String w2;
    protected String w3;

    public AbstractTrigram() {
        this.w1 = "?";
        this.w2 = "?";
        this.w3 = "?";
    }

    public AbstractTrigram(String w1, String w2, String w3) {
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
    abstract public int compareTo(AbstractTrigram other);

    @Override
    public String toString() {
        return this.w1 + "," + this.w2 + "," + this.w3;
    }
}
