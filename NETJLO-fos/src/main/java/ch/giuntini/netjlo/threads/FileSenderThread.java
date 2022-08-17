package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.packages.FilePartPackage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileSenderThread<S extends DefaultSocket> extends Thread implements AutoCloseable {

    private ObjectOutputStream objectOutputStream;
    private final S socket;
    private final ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<>();
    private final Object lock;
    private boolean stop;

    public FileSenderThread(S socket) {
        super("Sender-Thread");
        this.socket = socket;
        lock = new Object();
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (!stop) {
            synchronized (lock) {
                while (!files.isEmpty()) {
                    cycle(files.poll());
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            socket.shutdownOutput();
            //TODO check if stream isn't already closed through the shutdownOutput() method
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates .zip files when the given file is a directory or bigger then the zipThreshold
     *
     * @param file file to send
     */
    private void cycle(File file) {
        if (file != null) {
            try (FileReader fr = new FileReader(file)) {
                objectOutputStream.writeObject(new FilePartPackage(file.getName()));
                objectOutputStream.flush();
                char[] buff = new char[8192];
                while (fr.read(buff) > 0) {
                    objectOutputStream.writeObject(new FilePartPackage(String.valueOf(buff)));
                    objectOutputStream.flush();
                }
                objectOutputStream.writeObject(new FilePartPackage(true));
                objectOutputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void addFileToSendStack(File file) {
        if (file == null || !file.exists() || !file.canRead() || file.isDirectory())
            throw new IllegalArgumentException("The file to send can't be null, can't not exist can't be unreadable and can't be a directory");
        files.add(file);
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public synchronized void close() {
        stop = true;
        synchronized (lock) {
            lock.notify();
        }
    }
}
