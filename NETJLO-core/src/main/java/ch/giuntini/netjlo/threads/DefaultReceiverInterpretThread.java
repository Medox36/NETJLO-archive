package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultReceiverInterpretThread<I extends Interpretable<DefaultPackage>> extends Thread implements AutoCloseable {

    private final ConcurrentLinkedQueue<DefaultPackage> packages;
    private final I interpreter;
    private final Object lock;
    private boolean stop;

    public DefaultReceiverInterpretThread(ConcurrentLinkedQueue<DefaultPackage> packages, Object lock, Class<I> interpreterC)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super("Default-ReceiverInterpret-Thread");
        this.packages = packages;
        this.lock = lock;
        this.interpreter = interpreterC.getConstructor().newInstance();
    }

    @Override
    public void run() {
        synchronized (lock) {
            while (!stop) {
                while (!packages.isEmpty()) {
                    DefaultPackage p = packages.poll();
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
