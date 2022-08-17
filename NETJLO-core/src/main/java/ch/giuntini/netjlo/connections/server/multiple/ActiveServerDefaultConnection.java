package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.threads.DefaultReceiverThread;
import ch.giuntini.netjlo.threads.DefaultSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public class ActiveServerDefaultConnection<I extends Interpretable<DefaultPackage>> implements Disconnectable, Send<DefaultPackage> {

    private final Socket socket;
    private final DefaultSenderThread senderThread;
    private final DefaultReceiverThread<I> receiverThread;
    private final MultipleDefaultServerConnection<I> parent;

    protected ActiveServerDefaultConnection(Socket socket, Class<I> interpreter, MultipleDefaultServerConnection<I> parent) {
        if (socket.isClosed() || !socket.isConnected()) {
            throw new IllegalStateException("The given Socket for a ActiveServerConnection must be open and connected");
        }
        this.socket = socket;
        this.parent = parent;
        senderThread = new DefaultSenderThread(socket);
        senderThread.start();
        try {
            receiverThread = new DefaultReceiverThread<>(socket, interpreter);
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
            socket.close();
            parent.removeClosedActiveConnection(this);
        }
    }

    @Override
    public void send(DefaultPackage pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
