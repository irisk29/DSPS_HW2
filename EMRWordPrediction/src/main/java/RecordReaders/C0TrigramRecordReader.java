package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramC0;

public class C0TrigramRecordReader  extends AbstractTrigramRecordReader {
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramC0(w1, w2, w3);
    }
}
