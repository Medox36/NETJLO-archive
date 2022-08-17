package ch.giuntini.netjlo.connections.client;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.threads.ReceiverThread;
import ch.giuntini.netjlo.threads.SenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Connection<S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
        implements Connectable, Disconnectable, Send<P> {

    private S socket;
    private SenderThread<S, P> senderThread;
    private ReceiverThread<S, P, I> receiverThread;

    private Connection() {
    }

    public Connection(S socket, Class<I> interpreterC, Class<P> pack) {
        this.socket = socket;
        senderThread = new SenderThread<>(socket);
        try {
            receiverThread = new ReceiverThread<>(socket, interpreterC, pack);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
    public void send(P pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
