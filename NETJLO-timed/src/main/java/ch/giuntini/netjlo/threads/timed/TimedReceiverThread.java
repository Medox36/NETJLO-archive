package ch.giuntini.netjlo.threads.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;
import ch.giuntini.netjlo.streams.timed.TimedPackageObjectInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimedReceiverThread<S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
        extends Thread implements AutoCloseable {

    private final TimedReceiverInterpretThread<P, I> timedReceiverInterpretThread;
    private final ConcurrentLinkedQueue<P> packages = new ConcurrentLinkedQueue<>();
    private TimedPackageObjectInputStream<P> objectInputStream;
    private final S socket;
    private final Timer timer;
    private final Object lock;
    private boolean stop;

    public TimedReceiverThread(S socket, Class<I> interpreterC, Class<P> packC)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super("Timed-Receiving-Thread");
        this.socket = socket;
        try {
            objectInputStream = new TimedPackageObjectInputStream<>(new BufferedInputStream(socket.getInputStream()), packC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock = new Object();
        timer = new Timer("Daemon TimedPackage Receiver-Thread Timer", true);
        timedReceiverInterpretThread = new TimedReceiverInterpretThread<>(packages, lock, interpreterC);
    }

    @Override
    public void run() {
        timedReceiverInterpretThread.start();
        while (!stop) {
            try {
                @SuppressWarnings("unchecked")
                P p = (P) objectInputStream.readObject();
                packageValuation(p);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            socket.shutdownInput();
            //TODO check if stream isn't already closed through the shutdownOutput() method
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        timedReceiverInterpretThread.close();
    }

    private synchronized void packageValuation(P p) {
        Date now = Date.from(Instant.now());
        if (p.timeStamp == null || p.timeStamp.before(now)) {
            addForInterpretation(p);
        }
        if (p.timeStamp.after(now)) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    addForInterpretation(p);
                }
            }, p.timeStamp);
        }
    }

    private synchronized void addForInterpretation(P p) {
        packages.add(p);
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public synchronized void close() {
        stop = true;
    }
}
