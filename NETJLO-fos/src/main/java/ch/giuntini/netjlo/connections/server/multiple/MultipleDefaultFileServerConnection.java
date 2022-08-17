package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.interpreter.Interpretable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultipleDefaultFileServerConnection<I extends Interpretable> implements Acceptable, AutoCloseable {

    private final Class<I> interpreterC;
    private final String rootPathForFiles;

    private final ServerSocket serverSocket;
    private volatile int activeConnectionCount = 0;
    private volatile int maxConnectionCount = 5;
    private boolean stop;

    private final List<ActiveDefaultFileServerConnection<I>> CONNECTIONS = Collections.synchronizedList(new LinkedList<>());

    protected MultipleDefaultFileServerConnection(ServerSocket serverSocket, String rootPathForFiles, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        while (!stop) {
            while (activeConnectionCount < maxConnectionCount) {
                DefaultSocket socket = (DefaultSocket) serverSocket.accept();
                synchronized (CONNECTIONS) {
                    CONNECTIONS.add(new ActiveDefaultFileServerConnection<>(socket, rootPathForFiles, interpreterC));
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
