package ch.giuntini.netjlo.connections.client.sockets;

import ch.giuntini.netjlo.sockets.SocketUtils;

import java.net.*;

public class InetAddressSocket extends DefaultSocket {

    public InetAddressSocket(SocketImpl impl) throws SocketException {
        super(impl);
    }

    public InetAddressSocket(Inet4Address inet4Address, int port) {
        super(new InetSocketAddress(inet4Address, SocketUtils.checkPort(port)));
    }

    public InetAddressSocket(Inet6Address inet6Address, int port) {
        super(new InetSocketAddress(inet6Address, SocketUtils.checkPort(port)));
    }

    public InetAddressSocket(InetAddress inetAddress, int port) {
        super(new InetSocketAddress(inetAddress, SocketUtils.checkPort(port)));
    }

    public InetAddressSocket(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
    }
}
