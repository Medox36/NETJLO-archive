package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.FileReceiverThread;
import ch.giuntini.netjlo.threads.FileSenderThread;

import java.io.File;
import java.io.IOException;

public class ActiveDefaultFileServerConnection<I extends Interpretable> implements Disconnectable, Send {

    private final DefaultSocket socket;
    private final FileSenderThread<DefaultSocket> senderThread;
    private final FileReceiverThread<I> receiverThread;

    protected ActiveDefaultFileServerConnection(DefaultSocket socket, String rootPathForFiles, Class<I> interpreter) {
        if (socket.isClosed() || !socket.isConnected()) {
            throw new IllegalStateException("The given Socket for a ActiveServerConnection must be open and connected");
        }
        this.socket = socket;
        senderThread = new FileSenderThread<>(socket);
        senderThread.start();
        receiverThread = new FileReceiverThread<>(socket, rootPathForFiles, interpreter);
        receiverThread.start();
    }

    @Override
    public void disconnect() throws IOException {
        if (!socket.isClosed()) {
            senderThread.close();
            receiverThread.close();
            socket.close();
        }
    }

    @Override
    public void send(File file) {
        senderThread.addFileToSendStack(file);
    }
}
