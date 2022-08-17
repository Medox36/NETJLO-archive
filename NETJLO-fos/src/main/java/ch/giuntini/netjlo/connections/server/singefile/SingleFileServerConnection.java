package ch.giuntini.netjlo.connections.server.singefile;

import ch.giuntini.netjlo.connections.FileConnectionMode;
import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.threads.SimpleOneFileReceiverThread;
import ch.giuntini.netjlo.threads.SimpleOneFileSenderThread;

import java.io.File;
import java.io.IOException;

public class SingleFileServerConnection<I extends Interpretable>
        implements Acceptable, Disconnectable {

    private DefaultServerSocket serverSocket;
    private DefaultSocket socket;
    private FileConnectionMode mode;
    private String rootPathForFiles;
    private Class<I> interpreterC;
    private File file;

    private SingleFileServerConnection() {
    }

    public SingleFileServerConnection(DefaultServerSocket serverSocket, File file) {
        this.serverSocket = serverSocket;
        this.file = file;
        mode = FileConnectionMode.Send;
    }

    public SingleFileServerConnection(DefaultServerSocket serverSocket, String rootPathForFiles, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.interpreterC = interpreterC;
        mode = FileConnectionMode.Receive;
    }

    public FileConnectionMode getMode() {
        return mode;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = (DefaultSocket) serverSocket.accept();
        if (socket.isConnected()) {
            switch (mode) {
                case Send -> {
                    try {
                        SimpleOneFileSenderThread<DefaultSocket> senderThread = new SimpleOneFileSenderThread<>(socket, file);
                        socket.shutdownInput();
                        senderThread.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                case Receive -> {
                    try {
                        SimpleOneFileReceiverThread<I> receiverThread = new SimpleOneFileReceiverThread<>(socket, rootPathForFiles, interpreterC);
                        socket.shutdownOutput();
                        receiverThread.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        serverSocket.close();
        socket.disconnect();
    }
}
