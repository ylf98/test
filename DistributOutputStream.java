import java.io.*;

public class DistributOutputStream extends OutputStream {
    private OutputStream[] outputStreams = null;

    public DistributOutputStream(OutputStream[] outputStreams) {
        super();
        this.outputStreams = outputStreams;
    }

    @Override
    public void write(int v) throws IOException {
        for (OutputStream os : outputStreams) {
            try {
                os.write(v);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream os : outputStreams) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream os : outputStreams) {
            try {
                os.flush();
            } catch (IOException e) {
            }
        }
    }
}
