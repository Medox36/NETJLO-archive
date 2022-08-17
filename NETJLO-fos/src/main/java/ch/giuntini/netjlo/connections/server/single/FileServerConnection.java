package ch.giuntini.netjlo.connections.server.single;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.FileReceiverThread;
import ch.giuntini.netjlo.threads.FileSenderThread;

import java.io.File;
import java.io.IOException;

public class FileServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, I extends Interpretable>
        implements Acceptable, Disconnectable, Send {

    private S socket;
    private T serverSocket;
    private Class<I> interpreter;
    private String rootPathForFiles;
    private FileSenderThread<S> senderThread;
    private FileReceiverThread<I> receiverThread;

    private FileServerConnection() {
    }

    public FileServerConnection(T serverSocket, String rootPathForFiles, Class<I> interpreter) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.interpreter = interpreter;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new FileSenderThread<>(socket);
            senderThread.start();
            receiverThread = new FileReceiverThread<>(socket, rootPathForFiles, interpreter);
            receiverThread.start();
        }
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
