package ch.giuntini.netjlo.sockets;

import java.io.IOException;

public interface Disconnectable {

    void disconnect() throws IOException;
}
