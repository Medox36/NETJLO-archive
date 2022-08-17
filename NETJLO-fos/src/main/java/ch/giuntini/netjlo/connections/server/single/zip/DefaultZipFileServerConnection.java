package ch.giuntini.netjlo.connections.server.single.zip;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.zip.ZipFileReceiverThread;
import ch.giuntini.netjlo.threads.zip.ZipFileSenderThread;

import java.io.File;
import java.io.IOException;

public class DefaultZipFileServerConnection<I extends Interpretable> implements Acceptable, Disconnectable, Send {

    private DefaultSocket socket;
    private DefaultServerSocket serverSocket;
    private ZipFileSenderThread<DefaultSocket> senderThread;
    private ZipFileReceiverThread<I> receiverThread;
    private String rootPathForFiles;
    private long zipThreshold;
    private boolean unzipDirs;
    private Class<I> interpreterC;

    private DefaultZipFileServerConnection() {
    }

    public DefaultZipFileServerConnection(DefaultServerSocket serverSocket, String rootPathForFiles, Class<I> interpreterC) {
        this(serverSocket, rootPathForFiles, 536870912, true, interpreterC);
    }

    public DefaultZipFileServerConnection(DefaultServerSocket serverSocket, String rootPathForFiles, long zipThreshold, boolean unzipDirs, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.rootPathForFiles = rootPathForFiles;
        this.zipThreshold = zipThreshold;
        this.unzipDirs = unzipDirs;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = (DefaultSocket) serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new ZipFileSenderThread<>(socket, zipThreshold);
            senderThread.start();
            receiverThread = new ZipFileReceiverThread<>(socket, rootPathForFiles, interpreterC, unzipDirs);
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