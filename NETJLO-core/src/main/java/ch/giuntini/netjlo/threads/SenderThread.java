package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SenderThread<S extends DefaultSocket,P extends DefaultPackage> extends Thread implements AutoCloseable {

    private ObjectOutputStream objectOutputStream;
    private final S socket;
    private final ConcurrentLinkedQueue<P> packages = new ConcurrentLinkedQueue<>();
    private final Object lock;
    private boolean stop;

    public SenderThread(S socket) {
        super("Sender-Thread");
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
                        P p = packages.poll();
                        if (p != null) {
                            objectOutputStream.writeObject(p);
                            objectOutputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        close();
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

    public synchronized void addPackageToSendStack(P p) {
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
