package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultipleDefaultServerConnection<I extends Interpretable<DefaultPackage>>
        implements Acceptable, AutoCloseable {

    private final Class<I> interpreterC;

    private final ServerSocket serverSocket;
    private volatile int activeConnectionCount = 0;
    private volatile int maxConnectionCount = 5;
    private boolean stop;

    private final List<ActiveServerDefaultConnection<I>> CONNECTIONS = Collections.synchronizedList(new LinkedList<>());

    protected MultipleDefaultServerConnection(ServerSocket serverSocket, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        while (!stop) {
            while (activeConnectionCount < maxConnectionCount) {
                Socket socket = serverSocket.accept();
                synchronized (CONNECTIONS) {
                    CONNECTIONS.add(new ActiveServerDefaultConnection<>(socket, interpreterC, this));
                    activeConnectionCount++;
                }
            }
            Thread.onSpinWait();
        }
    }

    public void setMaxConnectionCount(int maxConnectionCount) {
        this.maxConnectionCount = maxConnectionCount;
    }

    public synchronized void removeClosedActiveConnection(ActiveServerDefaultConnection<I> connection) {
        synchronized (CONNECTIONS) {
            CONNECTIONS.remove(connection);
            activeConnectionCount--;
        }
    }

    public Class<?>[] getTypes() {
        return new Class[]{ServerSocket.class, Socket.class, DefaultPackage.class, interpreterC};
    }

    @Override
    public synchronized void close() throws Exception {
        stop = true;
    }
}
