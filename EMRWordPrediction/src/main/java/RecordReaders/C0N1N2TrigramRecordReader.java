package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramC0N1N2;

public class C0N1N2TrigramRecordReader extends AbstractTrigramRecordReader {
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramC0N1N2(w1, w2, w3);
    }
}
