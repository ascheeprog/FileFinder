package programmUtils;

import java.io.IOException;
import java.io.InputStream;

public class ProgressBytes extends InputStream implements AutoCloseable {
    private long readBytes = 0;
    private final InputStream reader;

    public ProgressBytes(InputStream reader) {
        this.reader = reader;
    }


    @Override
    public int read() throws IOException {
        int result = reader.read();
        if (result != -1) {
            readBytes++;
        }
        return result;
    }
    @Override
    public void close() throws IOException {
        super.close();
        reader.close();
    }

    public long getReadBytes() {
        return readBytes;
    }
}