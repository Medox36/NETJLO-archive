package ch.giuntini.netjlo.connections.client.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.TimedSend;
import ch.giuntini.netjlo.threads.timed.TimedReceiverThread;
import ch.giuntini.netjlo.threads.timed.TimedSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class TimedConnection<S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
        implements Connectable, Disconnectable, TimedSend<P> {

    private S socket;
    private TimedSenderThread<S, P> senderThread;
    private TimedReceiverThread<S, P, I> receiverThread;

    private TimedConnection() {
    }

    public TimedConnection(S socket, Class<I> interpreterC, Class<P> packC) {
        this.socket = socket;
        senderThread = new TimedSenderThread<>(socket);
        try {
            receiverThread = new TimedReceiverThread<>(socket, interpreterC, packC);
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

    @Override
    public void send(P pack, int delay) {
        senderThread.addPackageToSendStack(pack, delay);
    }

    @Override
    public void send(P pack, Date timestamp) {
        senderThread.addPackageToSendStack(pack, timestamp);
    }
}
