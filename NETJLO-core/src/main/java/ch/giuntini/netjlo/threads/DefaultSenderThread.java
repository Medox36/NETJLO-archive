package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultSenderThread extends Thread implements AutoCloseable {

    private ObjectOutputStream objectOutputStream;
    private final Socket socket;
    private final ConcurrentLinkedQueue<DefaultPackage> packages = new ConcurrentLinkedQueue<>();
    private final Object lock;
    private boolean stop;

    public DefaultSenderThread(Socket socket) {
        super("Default-Sender-Thread");
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock = new Object();
    }

    @Override
    public void run() {
        while (!stop) {
            synchronized (lock) {
                while (!packages.isEmpty()) {
                    try {
                        DefaultPackage p = packages.poll();
                        if (p != null) {
                            objectOutputStream.writeObject(p);
                            objectOutputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public synchronized void addPackageToSendStack(DefaultPackage p) {
        packages.add(p);
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
