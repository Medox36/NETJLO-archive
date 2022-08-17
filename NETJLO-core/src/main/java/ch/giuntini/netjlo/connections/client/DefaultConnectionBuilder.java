package ch.giuntini.netjlo.connections.client;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.net.SocketException;

public class DefaultConnectionBuilder<I extends Interpretable<DefaultPackage>> {

    private Class<I> interpreterC;
    private DefaultSocket socket;
    private String address;
    private int port;

    private boolean addressIsSet, portIsSet, interpreterIsSet, socketInstantiated;

    public DefaultConnectionBuilder() {
    }

    public DefaultConnectionBuilder<I> address(String address) {
        this.address = address;
        addressIsSet = true;
        return this;
    }

    public DefaultConnectionBuilder<I> port(int port) {
        this.port = port;
        portIsSet = true;
        return this;
    }

    public DefaultConnectionBuilder<I> interpreter(Class<I> interpreterC) {
        this.interpreterC = interpreterC;
        interpreterIsSet = true;
        return this;
    }

    public DefaultConnectionBuilder<I> soTimeout(int timeout) throws SocketException {
        checkState();
        socket.setSoTimeout(timeout);
        return this;
    }

    public DefaultConnectionBuilder<I> tcpNoDelay(boolean on) throws SocketException {
        checkState();
        socket.setTcpNoDelay(on);
        return this;
    }

    public DefaultConnectionBuilder<I> oobInline(boolean on) throws SocketException {
        checkState();
        socket.setOOBInline(on);
        return this;
    }

    public DefaultConnectionBuilder<I> keepAlive(boolean on) throws SocketException {
        checkState();
        socket.setKeepAlive(on);
        return this;
    }

    public DefaultConnection<I> build() {
        checkState();
        return new DefaultConnection<I>(socket, interpreterC);
    }

    private void checkState() {
        if (!addressIsSet || !portIsSet) {
            throw new IllegalStateException("The IP-Address or port hasn't been defined! IP-Address:" + address + " port:" + port);
        }
        if (!interpreterIsSet) {
            throw new IllegalStateException("The Interpreter class has not been set");
        }
        if (!socketInstantiated) {
            socket = new DefaultSocket(address, port);
            socketInstantiated = true;
        }
    }
}
