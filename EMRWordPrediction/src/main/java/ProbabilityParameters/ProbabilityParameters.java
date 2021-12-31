package ProbabilityParameters;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ProbabilityParameters implements Writable {
    private LongWritable N1;
    private LongWritable N2;
    private LongWritable N3;
    private LongWritable C0;
    private LongWritable C1;
    private LongWritable C2;

    public ProbabilityParameters() {
        this.N1 = new LongWritable();
        this.N2 = new LongWritable();
        this.N3 = new LongWritable();
        this.C0 = new LongWritable();
        this.C1 = new LongWritable();
        this.C2 = new LongWritable();
    }

    public ProbabilityParameters(LongWritable N1, LongWritable N2, LongWritable N3, LongWritable C0, LongWritable C1, LongWritable C2) {
        this.N1 = N1;
        this.N2 = N2;
        this.N3 = N3;
        this.C0 = C0;
        this.C1 = C1;
        this.C2 = C2;
    }

    public LongWritable getN1() {
        return N1;
    }

    public void setN1(LongWritable n1) {
        N1 = n1;
    }

    public LongWritable getN2() {
        return N2;
    }

    public void setN2(LongWritable n2) {
        N2 = n2;
    }

    public LongWritable getN3() {
        return N3;
    }

    public void setN3(LongWritable n3) {
        N3 = n3;
    }

    public LongWritable getC0() {
        return C0;
    }

    public void setC0(LongWritable c0) {
        C0 = c0;
    }

    public LongWritable getC1() {
        return C1;
    }

    public void setC1(LongWritable c1) {
        C1 = c1;
    }

    public LongWritable getC2() {
        return C2;
    }

    public void setC2(LongWritable c2) {
        C2 = c2;
    }

    public void readFields(DataInput in) throws IOException {
        this.N1 = new LongWritable(in.readLong());
        this.N2 = new LongWritable(in.readLong());
        this.N3 = new LongWritable(in.readLong());
        this.C0 = new LongWritable(in.readLong());
        this.C1 = new LongWritable(in.readLong());
        this.C2 = new LongWritable(in.readLong());
    }

    public void write(DataOutput out) throws IOException {
        out.writeLong(this.N1.get());
        out.writeLong(this.N2.get());
        out.writeLong(this.N3.get());
        out.writeLong(this.C0.get());
        out.writeLong(this.C1.get());
        out.writeLong(this.C2.get());
    }

    @Override
    public String toString() {
        return this.N1.toString() + "," + this.N2.toString() + "," + this.N3.toString()
                + "," + this.C0.toString() + "," + this.C1.toString() + "," + this.C2.toString();
    }

    public double calcProb()
    {
        double k3 = (Math.log(N3.get() + 1) + 1) / (Math.log(N3.get() + 1) + 2);
        double k2 = (Math.log(N2.get() + 1) + 1) / (Math.log(N2.get() + 1) + 2);

        return k3 * (((double)N3.get())/ C2.get()) +
                (1 - k3) * k2 * (((double)N2.get()) / C1.get()) +
                (1 - k3) * (1 - k2) * (((double)N1.get()) / C0.get());
    }
}
