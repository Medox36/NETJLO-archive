package ch.giuntini.netjlo.connections.server.single;

import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.ReceiverThread;
import ch.giuntini.netjlo.threads.SenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
        implements Acceptable, Disconnectable, Send<P> {

    private S socket;
    private T serverSocket;
    private Class<P> pack;
    private Class<I> interpreter;
    private SenderThread<S, P> senderThread;
    private ReceiverThread<S, P, I> receiverThread;

    private ServerConnection() {
    }

    public ServerConnection(T serverSocket, Class<I> interpreter, Class<P> pack) {
        this.serverSocket = serverSocket;
        this.interpreter = interpreter;
        this.pack = pack;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new SenderThread<>(socket);
            senderThread.start();
            try {
                receiverThread = new ReceiverThread<>(socket, interpreter, pack);
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
    public void send(P pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
