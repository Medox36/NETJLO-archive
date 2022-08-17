package ch.giuntini.netjlo.connections.server.multiple;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.DefaultPackage;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Prototype
 * <p>Use with caution
 */
//TODO try fix this generic mess
    @Deprecated
public class MultipleServerConnectionHolder {

    @SuppressWarnings("rawtypes")
    private static final List<MultipleServerConnection> INSTANCES = new ArrayList<>(0);

    @SuppressWarnings("rawtypes")
    private static MultipleDefaultServerConnection instance;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
    MultipleServerConnection<T, S, P, I>
    getInstance(T serverSocket, Class<T> serverSocketC, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        synchronized (INSTANCES) {
            Class<?>[] array = new Class[]{serverSocketC, socketC, packC, interpreterC};
            for (MultipleServerConnection connection : INSTANCES) {
                if (Arrays.equals(connection.getTypes(), array)) {
                    return connection;
                }
            }
            MultipleServerConnection<T, S, P, I> connection =
                    new MultipleServerConnection<>(serverSocket, serverSocketC, socketC, packC, interpreterC);
            INSTANCES.add(connection);
            return connection;
        }
    }

    @SuppressWarnings({"unchecked", "Convert2Diamond"})
    public static
    <I extends Interpretable<DefaultPackage>>
    MultipleDefaultServerConnection<I>
    getDefaultInstance(ServerSocket serverSocket, Class<I> interpreterC) {
        if (instance == null) {
            instance = new MultipleDefaultServerConnection<I>(serverSocket, interpreterC);
        }
        return (MultipleDefaultServerConnection<I>) instance;
    }

    @SuppressWarnings("unchecked")
    public static
    <I extends Interpretable<DefaultPackage>>
    MultipleDefaultServerConnection<I>
    retrieveDefaultInstance() {
        if (instance == null) {
            throw new NoSuchElementException("There is no default MultipleServerDefaultConnection instance");
        }
        return (MultipleDefaultServerConnection<I>) instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
    MultipleServerConnection<T, S, P, I>
    getInstance(Class<T> serverSocketC, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        synchronized (INSTANCES) {
            Class<?>[] array = new Class[]{serverSocketC, socketC, packC, interpreterC};
            for (MultipleServerConnection connection : INSTANCES) {
                if (Arrays.equals(connection.getTypes(), array)) {
                    return connection;
                }
            }
            throw new NoSuchElementException("There is no MultipleServerConnection " +
                    "with the given generic types/classes ");
        }
    }

    @SuppressWarnings("rawtypes")
    private static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
    boolean
    checkInstance(MultipleServerConnection<T, S, P, I> connection) {
        synchronized (INSTANCES) {
            Class<?>[] arr = connection.getTypes();
            for (MultipleServerConnection con : INSTANCES) {
                if (Arrays.equals(con.getTypes(), arr)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends DefaultPackage, I extends Interpretable<P>>
    void
    addInstance(MultipleServerConnection<T, S, P, I> connection) {
        synchronized (INSTANCES) {
            if (checkInstance(connection))
                throw new IllegalStateException("An Instance of a MultipleServerConnection " +
                        "with the given generic types already exists");
            INSTANCES.add(connection);
        }
    }
}
