package ProbabilityParameters;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ProbabilityParameters implements Writable {
    private IntWritable N1;
    private IntWritable N2;
    private IntWritable N3;
    private IntWritable C0;
    private IntWritable C1;
    private IntWritable C2;

    public ProbabilityParameters() {
        this.N1 = new IntWritable();
        this.N2 = new IntWritable();
        this.N3 = new IntWritable();
        this.C0 = new IntWritable();
        this.C1 = new IntWritable();
        this.C2 = new IntWritable();
    }

    public ProbabilityParameters(IntWritable N1, IntWritable N2, IntWritable N3, IntWritable C0, IntWritable C1, IntWritable C2) {
        this.N1 = N1;
        this.N2 = N2;
        this.N3 = N3;
        this.C0 = C0;
        this.C1 = C1;
        this.C2 = C2;
    }

    public IntWritable getN1() {
        return N1;
    }

    public void setN1(IntWritable n1) {
        N1 = n1;
    }

    public IntWritable getN2() {
        return N2;
    }

    public void setN2(IntWritable n2) {
        N2 = n2;
    }

    public IntWritable getN3() {
        return N3;
    }

    public void setN3(IntWritable n3) {
        N3 = n3;
    }

    public IntWritable getC0() {
        return C0;
    }

    public void setC0(IntWritable c0) {
        C0 = c0;
    }

    public IntWritable getC1() {
        return C1;
    }

    public void setC1(IntWritable c1) {
        C1 = c1;
    }

    public IntWritable getC2() {
        return C2;
    }

    public void setC2(IntWritable c2) {
        C2 = c2;
    }

    public void readFields(DataInput in) throws IOException {
        this.N1 = new IntWritable(in.readInt());
        this.N2 = new IntWritable(in.readInt());
        this.N3 = new IntWritable(in.readInt());
        this.C0 = new IntWritable(in.readInt());
        this.C1 = new IntWritable(in.readInt());
        this.C2 = new IntWritable(in.readInt());
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(this.N1.get());
        out.writeInt(this.N2.get());
        out.writeInt(this.N3.get());
        out.writeInt(this.C0.get());
        out.writeInt(this.C1.get());
        out.writeInt(this.C2.get());
    }

    @Override
    public String toString() {
        return this.N1.toString() + "," + this.N2.toString() + "," + this.N3.toString()
                + "," + this.C0.toString() + "," + this.C1.toString() + "," + this.C2.toString();
    }
}
