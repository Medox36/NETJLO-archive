package ch.giuntini.netjlo.connections.server.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DefaultServerSocket extends ServerSocket implements AutoCloseable {

    public DefaultServerSocket(int port) throws IOException {
        super(port);
    }

    public DefaultServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);
    }

    public DefaultServerSocket(int port, int backlog, int soTimeout) throws IOException {
        super(port, backlog);
        setSoTimeout(soTimeout);
    }

    public DefaultServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        super(port, backlog, bindAddr);
    }

    @Override
    public void close() throws IOException {
        if (!isClosed())
            super.close();
    }
}
