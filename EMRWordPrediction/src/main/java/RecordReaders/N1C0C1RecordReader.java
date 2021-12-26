package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramN1C0C1;

public class N1C0C1RecordReader  extends AbstractTrigramRecordReader{
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramN1C0C1(w1, w2, w3);
    }
}
