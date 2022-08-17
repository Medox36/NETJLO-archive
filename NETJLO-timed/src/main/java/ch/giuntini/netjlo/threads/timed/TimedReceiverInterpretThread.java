package ch.giuntini.netjlo.threads.timed;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;

class TimedReceiverInterpretThread<T extends TimedPackage, I extends Interpretable<T>>
        extends Thread implements AutoCloseable {

    private final ConcurrentLinkedQueue<T> packages;
    private final I interpreter;
    private final Object lock;
    private boolean stop;

    public TimedReceiverInterpretThread(ConcurrentLinkedQueue<T> packages, Object lock, Class<I> interpreterC)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super("Timed-ReceiverInterpret-Thread");
        this.packages = packages;
        this.lock = lock;
        this.interpreter = interpreterC.getConstructor().newInstance();
    }

    @Override
    public void run() {
        synchronized (lock) {
            while (!stop) {
                while (!packages.isEmpty()) {
                    T p = packages.poll();
                    if (p != null) {
                        interpreter.interpret(p);
                    }
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
