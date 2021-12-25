package RecordReaders;

import Trigrams.AbstractTrigram;
import Trigrams.TrigramN3C2;

public class N3C2TrigramRecordReader extends AbstractTrigramRecordReader {
    @Override
    protected AbstractTrigram buildTrigram(String w1, String w2, String w3) {
        return new TrigramN3C2(w1, w2, w3);
    }
}
