package ch.giuntini.netjlo.connections.server.sockets;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImpl;

public class CustomServerSocket<S extends DefaultSocket> extends DefaultServerSocket {

    private final Class<S> socketC;

    public CustomServerSocket(int port, Class<S> socketC) throws IOException {
        super(port);
        this.socketC = socketC;
    }

    public CustomServerSocket(int port, int backlog, Class<S> socketC) throws IOException {
        super(port, backlog);
        this.socketC = socketC;
    }

    public CustomServerSocket(int port, int backlog, int soTimeout, Class<S> socketC) throws IOException {
        super(port, backlog);
        setSoTimeout(soTimeout);
        this.socketC = socketC;
    }

    public CustomServerSocket(int port, int backlog, InetAddress bindAddr, Class<S> socketC) throws IOException {
        super(port, backlog, bindAddr);
        this.socketC = socketC;
    }

    @Override
    public S accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");
        try {
            S s = socketC.getConstructor(SocketImpl.class).newInstance((SocketImpl) null);
            implAccept(s);
            return s;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
