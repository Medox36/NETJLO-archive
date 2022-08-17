package ch.giuntini.netjlo.connections.client;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.sockets.Connectable;
import ch.giuntini.netjlo.sockets.Disconnectable;
import ch.giuntini.netjlo.sockets.Send;
import ch.giuntini.netjlo.packages.DefaultPackage;
import ch.giuntini.netjlo.threads.DefaultReceiverThread;
import ch.giuntini.netjlo.threads.DefaultSenderThread;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DefaultConnection<I extends Interpretable<DefaultPackage>> implements Connectable, Disconnectable, Send<DefaultPackage> {
    private DefaultSocket socket;
    private DefaultSenderThread senderThread;
    private DefaultReceiverThread<I> receiverThread;

    private DefaultConnection() {
    }

    public DefaultConnection(DefaultSocket socket, Class<I> interpreterC) {
        this.socket = socket;
        senderThread = new DefaultSenderThread(socket);
        try {
            receiverThread = new DefaultReceiverThread<>(socket, interpreterC);
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
            socket.close();
        }
    }

    @Override
    public void send(DefaultPackage pack) {
        senderThread.addPackageToSendStack(pack);
    }
}
