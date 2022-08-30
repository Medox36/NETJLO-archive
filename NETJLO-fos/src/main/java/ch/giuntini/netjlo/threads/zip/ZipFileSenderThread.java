package ch.giuntini.netjlo.threads.zip;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.packages.FilePartPackage;
import ch.giuntini.netjlo.zip.ZipUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZipFileSenderThread<S extends DefaultSocket> extends Thread implements AutoCloseable {

    private ObjectOutputStream objectOutputStream;
    private final S socket;
    private final ConcurrentLinkedQueue<File> files = new ConcurrentLinkedQueue<>();
    private final Object lock;
    private final long zipThreshold;
    private boolean stop;

    /**
     * creates a ZipFileSenderThread with the zipThreshold of half a Gibibyte (in Windows equivalent of Gigabyte)
     *
     * @param socket
     */
    public ZipFileSenderThread(S socket) {
        this(socket, 536870912);
    }

    /**
     * creates a ZipFileSenderThread which zips files before sending them when the filesize in bytes is above the threshold
     *
     * @param socket
     * @param zipThreshold for filesize in bytes over wich files get zipped
     */
    public ZipFileSenderThread(S socket, long zipThreshold) {
        super("Sender-Thread");
        this.socket = socket;
        this.zipThreshold = zipThreshold;
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
            final boolean isDir = file.isDirectory();
            if (!file.getName().endsWith(".zip")) {
                file = zipFile(file, isDir);
            }
            try (FileReader fr = new FileReader(file)) {
                objectOutputStream.writeObject(new FilePartPackage(file.getName()));
                objectOutputStream.writeObject(new FilePartPackage(String.valueOf(isDir)));
                objectOutputStream.flush();
                char[] buff = new char[8192];
                while (fr.read(buff) > 0) {
                    objectOutputStream.writeObject(new FilePartPackage(String.valueOf(buff)));
                    objectOutputStream.flush();
                }
                objectOutputStream.writeObject(new FilePartPackage(true));
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * zips the given file if its length is bigger than the threshold
     *
     * @param file file to zip
     * @param isDir true if the file is a directory, otherwise false
     * @return the (zipped) file
     */
    private File zipFile(File file, final boolean isDir) {
        if (file.length() > zipThreshold) {
            try {
                if (isDir) {
                    file = ZipUtil.zipDirAndSubDirs(file);
                } else {
                    file = ZipUtil.zipSingleFile(file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    public synchronized void addFileToSendStack(File file) {
        if (file == null || !file.exists() || !file.canRead())
            throw new IllegalArgumentException("The file to send can't be null, can't not exist and can't be unreadable");
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