package ch.giuntini.netjlo.threads.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.packages.timed.TimedPackage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimedSenderThread<S extends DefaultSocket, P extends TimedPackage> extends Thread implements AutoCloseable {

    private ObjectOutputStream objectOutputStream;
    private final ConcurrentLinkedQueue<P> packages = new ConcurrentLinkedQueue<>();
    private final S socket;
    private final Timer timer;
    private final Object lock;
    private boolean stop;

    public TimedSenderThread(S socket) {
        super("Timed-Sender-Thread");
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer = new Timer("Daemon TimedPackage Sender-Thread Timer", true);
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

    public synchronized void addPackageToSendStack(P p, Date timestamp) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                addPackageToSendStack(p);
            }
        }, timestamp);
    }

    /**
     *
     * @param p package to send
     * @param delay in milliseconds
     */
    public synchronized void addPackageToSendStack(P p, int delay) {
        addPackageToSendStack(p, Date.from(Instant.now().plus(delay, ChronoUnit.MILLIS)));
    }

    @Override
    public synchronized void close() {
        stop = true;
        synchronized (lock) {
            lock.notify();
        }
    }
}
