package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.ReceiverThread;
import ch.giuntini.netjlo.threads.SenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ActiveServerConnection<S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
        implements Disconnectable, Send<P> {

    private final S socket;
    private final SenderThread<S, P> senderThread;
    private final ReceiverThread<S, P, I> receiverThread;
    private final MultipleServerConnection<? extends DefaultServerSocket, S, P, I> parent;


    protected ActiveServerConnection(S socket, Class<I> interpreter, Class<P> pack, MultipleServerConnection<? extends DefaultServerSocket, S, P, I> parent) {
        if (socket.isClosed() || !socket.isConnected()) {
            throw new IllegalStateException("The given Socket for a ActiveServerConnection must be open and connected");
        }
        this.socket = socket;
        this.parent = parent;
        senderThread = new SenderThread<>(socket);
        senderThread.start();
        try {
            receiverThread = new ReceiverThread<>(socket, interpreter, pack);
            receiverThread.start();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (!socket.isClosed()) {
            senderThread.close();
            receiverThread.close();
            socket.disconnect();
            parent.removeClosedActiveConnection(this);
        }
    }

    @Override
    public void send(P pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
