package ch.giuntini.netjlo.connections.server.single;

import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.IOException;
import java.net.SocketException;

public class DefaultServerConnectionBuilder<I extends Interpretable<DefaultPackage>> {

    private DefaultServerSocket socket;

    /**
     * set to the JDK default, as used in the mentioned constructor below
     *
     * @see java.net.ServerSocket#ServerSocket(int)  ServerSocket
     */
    private int backlog = 50;
    private int port;
    private Class<I> interpreterC;

    private boolean portIsSet, interpreterIsSet, socketInstantiated;

    public DefaultServerConnectionBuilder() {
    }

    public DefaultServerConnectionBuilder<I> port(int port) {
        this.port = port;
        portIsSet = true;
        return this;
    }

    public DefaultServerConnectionBuilder<I> backlog(int backlog) {
        if (socketInstantiated)
            throw new IllegalStateException("Backlog can't be set when the DefaultServerSocket is already created. Try setting the backlog earlier.");
        this.backlog = backlog;
        return this;
    }

    public DefaultServerConnectionBuilder<I> interpreter(Class<I> interpreterC) {
        this.interpreterC = interpreterC;
        interpreterIsSet = true;
        return this;
    }

    public DefaultServerConnectionBuilder<I> soTimeout(int timeout) throws SocketException {
        checkState();
        socket.setSoTimeout(timeout);
        return this;
    }

    public DefaultServerConnection<I> build() {
        checkState();
        return new DefaultServerConnection<I>(socket, interpreterC);
    }

    private void checkState() {
        if (!portIsSet) {
            throw new IllegalStateException("The port hasn't been defined! port:" + port);
        }
        if (!interpreterIsSet) {
            throw new IllegalStateException("The Interpreter class has not been set");
        }
        if (!socketInstantiated) {
            try {
                socket = new DefaultServerSocket(port, backlog);
                socketInstantiated = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
