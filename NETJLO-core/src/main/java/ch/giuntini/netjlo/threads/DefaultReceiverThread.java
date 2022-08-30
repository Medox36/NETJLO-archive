package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.streams.DefaultPackageObjectInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultReceiverThread<I extends Interpretable<DefaultPackage>> extends Thread implements AutoCloseable {

    private final DefaultReceiverInterpretThread<I> defaultReceiverInterpretThread;
    private final Socket socket;

    private final ConcurrentLinkedQueue<DefaultPackage> packages = new ConcurrentLinkedQueue<>();
    private DefaultPackageObjectInputStream objectInputStream;
    private final Object lock;
    private boolean stop;

    public DefaultReceiverThread(Socket socket, Class<I> interpreterC)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super("Default-Receiving-Thread");
        this.socket = socket;
        try {
            objectInputStream = new DefaultPackageObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock = new Object();
        defaultReceiverInterpretThread = new DefaultReceiverInterpretThread<>(packages, lock, interpreterC);
    }

    @Override
    public void run() {
        defaultReceiverInterpretThread.start();
        while (!stop) {
            try {
                DefaultPackage p = (DefaultPackage) objectInputStream.readObject();
                packages.add(p);
                synchronized (lock) {
                    lock.notify();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                close();
            }
        }
        try {
            socket.shutdownInput();
            //TODO check if stream isn't already closed through the shutdownInput() method
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        defaultReceiverInterpretThread.close();
    }

    @Override
    public synchronized void close() {
        stop = true;
    }
}
