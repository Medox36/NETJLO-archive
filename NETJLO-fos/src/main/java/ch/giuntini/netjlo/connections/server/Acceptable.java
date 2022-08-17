package ch.giuntini.netjlo.connections.server;

import java.io.IOException;

public interface Acceptable {

    void acceptAndWait() throws IOException;
}
