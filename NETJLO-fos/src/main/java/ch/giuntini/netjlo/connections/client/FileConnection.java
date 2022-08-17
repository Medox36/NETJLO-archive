package ch.giuntini.netjlo.connections.client;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.FileReceiverThread;
import ch.giuntini.netjlo.threads.FileSenderThread;

import java.io.File;
import java.io.IOException;

public class FileConnection<S extends DefaultSocket, I extends Interpretable>
        implements Connectable, Disconnectable, Send {

    private S socket;
    private FileSenderThread<S> senderThread;
    private FileReceiverThread<I> receiverThread;

    private FileConnection() {
    }

    public FileConnection(S socket, String rootPathForFiles, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new FileSenderThread<>(socket);
        receiverThread = new FileReceiverThread<>(socket, rootPathForFiles, interpreterC);
    }

    @Override
    public void connect() throws IOException {
        socket.connect();
        senderThread.start();
        receiverThread.start();
    }

    @Override
    public void disconnect() throws IOException {
        if (!socket.isClosed()) {
            senderThread.close();
            receiverThread.close();
            socket.disconnect();
        }
    }

    @Override
    public void send(File file) {
        senderThread.addFileToSendStack(file);
    }
}