package ch.giuntini.netjlo.connections.client.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;

import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

public class TimedConnectionBuilder<S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>> {
    private Class<S> socketC;
    private Class<P> packC;
    private Class<I> interpreterC;
    private S socket;
    private String address;
    private int port;

    private boolean addressIsSet, portIsSet, socketIsSet, interpreterIsSet, packagesIsSet, socketInstantiated;

    public TimedConnectionBuilder() {
    }

    public TimedConnectionBuilder<S, P, I> address(String address) {
        this.address = address;
        addressIsSet = true;
        return this;
    }

    public TimedConnectionBuilder<S, P, I> port(int port) {
        this.port = port;
        portIsSet = true;
        return this;
    }

    public TimedConnectionBuilder<S, P, I> socket(Class<S> socketC) {
        this.socketC = socketC;
        socketIsSet = true;
        return this;
    }

    public TimedConnectionBuilder<S, P, I> interpreter(Class<I> interpreterC) {
        this.interpreterC = interpreterC;
        interpreterIsSet = true;
        return this;
    }

    public TimedConnectionBuilder<S, P, I> pack(Class<P> packC) {
        this.packC = packC;
        packagesIsSet = true;
        return this;
    }

    public TimedConnectionBuilder<S, P, I> soTimeout(int timeout) throws SocketException {
        checkState();
        socket.setSoTimeout(timeout);
        return this;
    }

    public TimedConnectionBuilder<S, P, I> tcpNoDelay(boolean on) throws SocketException {
        checkState();
        socket.setTcpNoDelay(on);
        return this;
    }

    public TimedConnectionBuilder<S, P, I> oobInline(boolean on) throws SocketException {
        checkState();
        socket.setOOBInline(on);
        return this;
    }

    public TimedConnectionBuilder<S, P, I> keepAlive(boolean on) throws SocketException {
        checkState();
        socket.setKeepAlive(on);
        return this;
    }

    public TimedConnection<S, P, I> build() {
        checkState();
        return new TimedConnection<>(socket, interpreterC, packC);
    }

    //@SuppressWarnings("unchecked")
    private void checkState() {
        if (!addressIsSet || !portIsSet) {
            throw new IllegalStateException("The IP-Address or port hasn't been defined! IP-Address:" + address + " port:" + port);
        }
        if (!socketIsSet) {
            throw new IllegalStateException("The Socket class has not been set");
        }
        if (!interpreterIsSet) {
            throw new IllegalStateException("The Interpreter class has not been set");
        }
        if (!packagesIsSet) {
            throw new IllegalStateException("The Package class has not been set");
        }
        if (!socketInstantiated) {
            try {
                this.socket = socketC.getConstructor(String.class, int.class).newInstance(address, port);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            socketInstantiated = true;
        }
    }
}
