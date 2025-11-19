package com.regnosys.rosetta.generator.java.st;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class ProducerInputStreams {
    private static final int BUFFER_SIZE = 16 * 1024;
    
    @FunctionalInterface public interface ThrowingConsumer<T> { void accept(T t) throws Exception; }

    /** Returns an InputStream that is produced by writing bytes to the given OutputStream in a daemon thread. */
    public static InputStream fromOutputStream(ThrowingConsumer<OutputStream> writeBytes) throws IOException {
        PipedInputStream pin = new PipedInputStream(BUFFER_SIZE);
        PipedOutputStream pout = new PipedOutputStream(pin);
        var err = new AtomicReference<Throwable>();
        var t = new Thread(() -> {
            try (pout) { 
                writeBytes.accept(pout);
            } catch (Throwable e) {
                err.set(e);
                try { pout.close(); } catch (Exception ignore) {}
            }
        }, "producer");
        t.setDaemon(true);
        t.start();

        // Wrap to surface producer errors and join on close
        return new FilterInputStream(pin) {
            @Override public void close() throws IOException {
                super.close();
                try { 
                    t.join();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                var e = err.get();
                if (e != null) throw new IOException("Producer failed", e);
            }
        };
    }
}
