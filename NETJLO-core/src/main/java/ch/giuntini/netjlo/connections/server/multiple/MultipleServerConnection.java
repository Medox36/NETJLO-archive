package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultipleServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
        implements Acceptable, AutoCloseable {

    private final Class<T> serverSocketC;
    private final Class<S> socketC;
    private final Class<P> packC;
    private final Class<I> interpreterC;

    private final T serverSocket;
    private volatile int activeConnectionCount = 0;
    private volatile int maxConnectionCount = 5;
    private boolean stop;

    private final List<ActiveServerConnection<S, P, I>> CONNECTIONS = Collections.synchronizedList(new LinkedList<>());

    public MultipleServerConnection(T serverSocket, Class<T> serverSocketC, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.serverSocketC = serverSocketC;
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
                    CONNECTIONS.add(new ActiveServerConnection<>(socket, interpreterC, packC, this));
                    activeConnectionCount++;
                }
            }
            Thread.onSpinWait();
        }
    }

    public void setMaxConnectionCount(int maxConnectionCount) {
        this.maxConnectionCount = maxConnectionCount;
    }

    public synchronized void removeClosedActiveConnection(ActiveServerConnection<S, P, I> connection) {
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
