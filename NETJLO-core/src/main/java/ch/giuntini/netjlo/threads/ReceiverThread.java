package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.streams.PackageObjectInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReceiverThread<S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
        extends Thread implements AutoCloseable {

    private final ReceiverInterpretThread<P, I> receiverInterpretThread;
    private final ConcurrentLinkedQueue<P> packages = new ConcurrentLinkedQueue<>();
    private PackageObjectInputStream<P> objectInputStream;
    private final S socket;
    private final Object lock;
    private boolean stop;

    public ReceiverThread(S socket, Class<I> interpreterC, Class<P> packC)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super("Receiving-Thread");
        this.socket = socket;
        try {
            objectInputStream = new PackageObjectInputStream<>(new BufferedInputStream(socket.getInputStream()), packC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock = new Object();
        receiverInterpretThread = new ReceiverInterpretThread<>(packages, lock, interpreterC);
    }

    @Override
    public void run() {
        receiverInterpretThread.start();
        while (!stop) {
            try {
                @SuppressWarnings("unchecked")
                P p = (P) objectInputStream.readObject();
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
        receiverInterpretThread.close();
    }

    @Override
    public synchronized void close() {
        stop = true;
    }
}
