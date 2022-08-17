package ch.giuntini.netjlo.connections.server.multiple.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.sockets.DefaultServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.TimedSend;
import ch.giuntini.netjlo.threads.timed.TimedReceiverThread;
import ch.giuntini.netjlo.threads.timed.TimedSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class TimedActiveServerConnection<S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
        implements Disconnectable, TimedSend<P> {

    private final S socket;
    private final TimedSenderThread<S, P> senderThread;
    private final TimedReceiverThread<S, P, I> receiverThread;
    private final TimedMultipleServerConnection<? extends DefaultServerSocket, S, P, I> parent;


    protected TimedActiveServerConnection(S socket, Class<I> interpreter, Class<P> pack, TimedMultipleServerConnection<? extends DefaultServerSocket, S, P, I> parent) {
        if (socket.isClosed() || !socket.isConnected()) {
            throw new IllegalStateException("The given Socket for a ActiveServerConnection must be open and connected");
        }
        this.socket = socket;
        this.parent = parent;
        senderThread = new TimedSenderThread<>(socket);
        senderThread.start();
        try {
            receiverThread = new TimedReceiverThread<>(socket, interpreter, pack);
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

    @Override
    public void send(P pack, int delay) {
        senderThread.addPackageToSendStack(pack, delay);
    }

    @Override
    public void send(P pack, Date timestamp) {
        senderThread.addPackageToSendStack(pack, timestamp);
    }
}
