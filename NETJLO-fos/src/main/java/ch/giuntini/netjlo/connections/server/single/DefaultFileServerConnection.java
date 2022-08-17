package ch.giuntini.netjlo.connections.server.single;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.FileReceiverThread;
import ch.giuntini.netjlo.threads.FileSenderThread;

import java.io.File;
import java.io.IOException;

public class DefaultFileServerConnection<I extends Interpretable> implements Acceptable, Disconnectable, Send {

    private DefaultSocket socket;
    private DefaultServerSocket serverSocket;
    private FileSenderThread<DefaultSocket> senderThread;
    private FileReceiverThread<I> receiverThread;
    private Class<I> interpreterC;
    private String rootPathForFiles;

    private DefaultFileServerConnection() {
    }

    public DefaultFileServerConnection(DefaultServerSocket serverSocket, String rootPathForFiles, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = (DefaultSocket) serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new FileSenderThread<>(socket);
            senderThread.start();
            receiverThread = new FileReceiverThread<>(socket, rootPathForFiles, interpreterC);
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
