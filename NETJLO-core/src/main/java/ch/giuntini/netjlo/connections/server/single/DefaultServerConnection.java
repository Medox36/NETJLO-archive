package ch.giuntini.netjlo.connections.server.single;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.DefaultReceiverThread;
import ch.giuntini.netjlo.threads.DefaultSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DefaultServerConnection<I extends Interpretable<DefaultPackage>>
        implements Acceptable, Disconnectable, Send<DefaultPackage> {

    private DefaultSocket socket;
    private DefaultServerSocket serverSocket;
    private DefaultSenderThread senderThread;
    private DefaultReceiverThread<I> receiverThread;
    private Class<I> interpreterC;

    private DefaultServerConnection() {
    }

    public DefaultServerConnection(DefaultServerSocket serverSocket, Class<I> interpreterC) {
        this.serverSocket = serverSocket;
        this.interpreterC = interpreterC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = (DefaultSocket) serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new DefaultSenderThread(socket);
            senderThread.start();
            try {
                receiverThread = new DefaultReceiverThread<>(socket, interpreterC);
                receiverThread.start();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
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
    public void send(DefaultPackage pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
