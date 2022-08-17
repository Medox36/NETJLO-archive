package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultipleFileServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, I extends Interpretable>
        implements Acceptable, AutoCloseable {

    private final Class<I> interpreterC;
    private final String rootPathForFiles;

    private final T serverSocket;
    private volatile int activeConnectionCount = 0;
    private volatile int maxConnectionCount = 5;
    private boolean stop;

    private final List<ActiveFileServerConnection<S, I>> CONNECTIONS = Collections.synchronizedList(new LinkedList<>());

    public MultipleFileServerConnection(T serverSocket, String rootPathForFiles, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        while (!stop) {
            while (activeConnectionCount < maxConnectionCount) {
                S socket = serverSocket.accept();
                synchronized (CONNECTIONS) {
                    CONNECTIONS.add(new ActiveFileServerConnection<>(socket, rootPathForFiles, interpreterC));
                    activeConnectionCount++;
                }
            }
            Thread.onSpinWait();
        }
    }

    public void setMaxConnectionCount(int maxConnectionCount) {
        this.maxConnectionCount = maxConnectionCount;
    }

    @Override
    public synchronized void close() throws Exception {
        stop = true;
    }
}
