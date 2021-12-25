package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramN2;

public class N2TrigramRecordReader extends AbstractTrigramRecordReader {
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramN2(w1, w2, w3);
    }
}
