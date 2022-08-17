package ch.giuntini.netjlo.connections.client.sockets;

import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.SocketUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;

public class DefaultSocket extends Socket implements Connectable, Disconnectable {

    private final InetSocketAddress address;

    public DefaultSocket(SocketImpl impl) throws SocketException {
        super(impl);
        address = null;
    }

    public DefaultSocket(String address, int port) {
        super();
        this.address = new InetSocketAddress(SocketUtils.checkIPAddress(address), SocketUtils.checkPort(port));
    }

    public DefaultSocket(InetSocketAddress address) {
        super();
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        if (!isConnected())
            connect(address);
    }

    @Override
    public void disconnect() throws IOException {
        if (!isClosed())
            close();
    }
}
