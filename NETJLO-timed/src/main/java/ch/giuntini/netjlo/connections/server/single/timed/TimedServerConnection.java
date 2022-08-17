package ch.giuntini.netjlo.connections.server.single.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.Acceptable;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.TimedSend;
import ch.giuntini.netjlo.threads.timed.TimedReceiverThread;
import ch.giuntini.netjlo.threads.timed.TimedSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class TimedServerConnection<T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
        implements Acceptable, Disconnectable, TimedSend<P> {

    private S socket;
    private T serverSocket;
    private Class<P> packC;
    private Class<I> interpreterC;
    private TimedSenderThread<S, P> senderThread;
    private TimedReceiverThread<S, P, I> receiverThread;

    private TimedServerConnection() {
    }

    public TimedServerConnection(T serverSocket, Class<I> interpreterC, Class<P> packC) {
        this.serverSocket = serverSocket;
        this.interpreterC = interpreterC;
        this.packC = packC;
    }

    @Override
    public void acceptAndWait() throws IOException {
        socket = serverSocket.accept();
        if (socket.isConnected()) {
            senderThread = new TimedSenderThread<>(socket);
            senderThread.start();
            try {
                receiverThread = new TimedReceiverThread<>(socket, interpreterC, packC);
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

    @Override
    public void send(P pack, int delay) {
        senderThread.addPackageToSendStack(pack, delay);
    }

    @Override
    public void send(P pack, Date timestamp) {
        senderThread.addPackageToSendStack(pack, timestamp);
    }
}
