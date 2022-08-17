package ch.giuntini.netjlo.connections.client.zip;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.zip.ZipFileReceiverThread;
import ch.giuntini.netjlo.threads.zip.ZipFileSenderThread;

import java.io.File;
import java.io.IOException;

public class ZipFileConnection <S extends DefaultSocket, I extends Interpretable>
        implements Connectable, Disconnectable, Send {

    private S socket;
    private ZipFileSenderThread<S> senderThread;
    private ZipFileReceiverThread<I> receiverThread;

    private ZipFileConnection() {
    }

    public ZipFileConnection(S socket, String rootPathForFiles, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new ZipFileSenderThread<>(socket);
        receiverThread = new ZipFileReceiverThread<>(socket, rootPathForFiles, interpreterC);
    }

    public ZipFileConnection(S socket, String rootPathForFiles, long zipThreshold, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new ZipFileSenderThread<>(socket, zipThreshold);
        receiverThread = new ZipFileReceiverThread<>(socket, rootPathForFiles, interpreterC);
    }

    public ZipFileConnection(S socket, String rootPathForFiles, boolean unzipDirs, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new ZipFileSenderThread<>(socket);
        receiverThread = new ZipFileReceiverThread<>(socket, rootPathForFiles, interpreterC, unzipDirs);
    }

    public ZipFileConnection(S socket, String rootPathForFiles, long zipThreshold, boolean unzipDirs, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new ZipFileSenderThread<>(socket, zipThreshold);
        receiverThread = new ZipFileReceiverThread<>(socket, rootPathForFiles, interpreterC, unzipDirs);
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