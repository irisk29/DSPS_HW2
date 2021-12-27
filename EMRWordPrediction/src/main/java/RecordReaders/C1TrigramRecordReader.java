package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramC1;

public class C1TrigramRecordReader  extends AbstractTrigramRecordReader {
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramC1(w1, w2, w3);
    }
}
