package ch.giuntini.netjlo.connections.server.multiple.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TimedMultipleServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
        implements Acceptable, AutoCloseable {

    private final Class<T> serverSocketC;
    private final Class<S> socketC;
    private final Class<P> packC;
    private final Class<I> interpreterC;

    private final T serverSocket;
    private volatile int activeConnectionCount = 0;
    private volatile int maxConnectionCount = 5;
    private boolean stop;

    private final List<TimedActiveServerConnection<S, P, I>> CONNECTIONS = Collections.synchronizedList(new LinkedList<>());


    @SuppressWarnings("unchecked")
    public TimedMultipleServerConnection(T serverSocket, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.serverSocketC = (Class<T>) serverSocket.getClass();
        this.socketC = socketC;
        this.packC = packC;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        while (!stop) {
            while (activeConnectionCount < maxConnectionCount) {
                S socket = serverSocket.accept();
                synchronized (CONNECTIONS) {
                    CONNECTIONS.add(new TimedActiveServerConnection<>(socket, interpreterC, packC, this));
                    activeConnectionCount++;
                }
            }
            Thread.onSpinWait();
        }
    }

    public void setMaxConnectionCount(int maxConnectionCount) {
        this.maxConnectionCount = maxConnectionCount;
    }

    public synchronized void removeClosedActiveConnection(TimedActiveServerConnection<S, P, I> connection) {
        synchronized (CONNECTIONS) {
            CONNECTIONS.remove(connection);
            activeConnectionCount--;
        }
    }

    public Class<?>[] getTypes() {
        return new Class[]{serverSocketC, socketC, packC, interpreterC};
    }

    @Override
    public synchronized void close() throws Exception {
        stop = true;
    }
}
