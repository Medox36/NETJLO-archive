package ch.giuntini.netjlo.connections.client;

import ch.giuntini.netjlo.connections.FileConnectionMode;
import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.threads.SimpleOneFileReceiverThread;
import ch.giuntini.netjlo.threads.SimpleOneFileSenderThread;

import java.io.File;
import java.io.IOException;

public class SingleFileConnection<S extends DefaultSocket, I extends Interpretable>
        implements Connectable, Disconnectable {

    private S socket;
    private SimpleOneFileSenderThread<S> senderThread;
    private SimpleOneFileReceiverThread<I> receiverThread;
    private FileConnectionMode mode;

    private SingleFileConnection() {
    }

    public SingleFileConnection(S socket, File file) {
        this.socket = socket;
        senderThread = new SimpleOneFileSenderThread<>(socket, file);
        mode = FileConnectionMode.Send;
        try {
            socket.shutdownInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SingleFileConnection(S socket, String rootPathForFiles, Class<I> interpreterC) {
        this.socket = socket;
        receiverThread = new SimpleOneFileReceiverThread<>(socket, rootPathForFiles, interpreterC);
        mode = FileConnectionMode.Receive;
        try {
            socket.shutdownOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileConnectionMode getMode() {
        return mode;
    }

    @Override
    public void connect() throws IOException {
        socket.connect();
        switch (mode) {
            case Send -> senderThread.start();
            case Receive -> receiverThread.start();
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (!socket.isClosed()) {
            socket.disconnect();
        }
    }
}
