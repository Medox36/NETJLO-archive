package ch.giuntini.netjlo.connections.server.multiple.timed;

import ch.giuntini.netjlo.connections.client.sockets.DefaultSocket;
import ch.giuntini.netjlo.connections.server.sockets.CustomServerSocket;
import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.timed.TimedPackage;

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
public class TimedMultipleServerConnectionHolder {

    @SuppressWarnings("rawtypes")
    private static final List<TimedMultipleServerConnection> INSTANCES = new ArrayList<>(0);

    @SuppressWarnings("rawtypes")
    private static TimedMultipleServerConnection defaultInstance;

    @SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"})
    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
    TimedMultipleServerConnection<T, S, P, I>
    getInstance(T serverSocket, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        synchronized (INSTANCES) {
            Class<?>[] array = new Class[]{(Class<T>) serverSocket.getClass(), socketC, packC, interpreterC};
            for (TimedMultipleServerConnection connection : INSTANCES) {
                if (Arrays.equals(connection.getTypes(), array)) {
                    return connection;
                }
            }
            TimedMultipleServerConnection<T, S, P, I> connection =
                    new TimedMultipleServerConnection<>(serverSocket, socketC, packC, interpreterC);
            INSTANCES.add(connection);
            return connection;
        }
    }

    @SuppressWarnings({"unchecked", "Convert2Diamond"})
    public static
    <I extends Interpretable<TimedPackage>>
    TimedMultipleServerConnection<CustomServerSocket<DefaultSocket>, DefaultSocket, TimedPackage, I>
    getDefaultInstance(CustomServerSocket<DefaultSocket> serverSocket, Class<I> interpreterC) {
        if (defaultInstance == null)
            defaultInstance = new TimedMultipleServerConnection<CustomServerSocket<DefaultSocket>, DefaultSocket, TimedPackage, I>(serverSocket, DefaultSocket.class, TimedPackage.class, interpreterC);
        return (TimedMultipleServerConnection<CustomServerSocket<DefaultSocket>, DefaultSocket, TimedPackage, I>) defaultInstance;
    }

    @SuppressWarnings("unchecked")
    public static
    <I extends Interpretable<TimedPackage>>
    TimedMultipleServerConnection<CustomServerSocket<DefaultSocket>, DefaultSocket, TimedPackage, I>
    retrieveDefaultInstance() {
        if (defaultInstance == null) {
            throw new NoSuchElementException("There is no default TimedMultipleServerDefaultConnection instance");
        }
        return (TimedMultipleServerConnection<CustomServerSocket<DefaultSocket>, DefaultSocket, TimedPackage, I>) defaultInstance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
    TimedMultipleServerConnection<T, S, P, I>
    getInstance(Class<T> serverSocketC, Class<S> socketC, Class<P> packC, Class<I> interpreterC) {
        synchronized (INSTANCES) {
            Class<?>[] array = new Class[]{serverSocketC, socketC, packC, interpreterC};
            for (TimedMultipleServerConnection connection : INSTANCES) {
                if (Arrays.equals(connection.getTypes(), array)) {
                    return connection;
                }
            }
            throw new NoSuchElementException("There is no TimedMultipleServerConnection " +
                    "with the given generic types/classes ");
        }
    }

    @SuppressWarnings("rawtypes")
    private static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
    boolean
    checkInstance(TimedMultipleServerConnection<T, S, P, I> connection) {
        synchronized (INSTANCES) {
            Class<?>[] arr = connection.getTypes();
            for (TimedMultipleServerConnection con : INSTANCES) {
                if (Arrays.equals(con.getTypes(), arr)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static
    <T extends CustomServerSocket<S>, S extends DefaultSocket, P extends TimedPackage, I extends Interpretable<P>>
    void
    addInstance(TimedMultipleServerConnection<T, S, P, I> connection) {
        synchronized (INSTANCES) {
            if (checkInstance(connection))
                throw new IllegalStateException("An Instance of a MultipleServerConnection " +
                        "with the given generic types already exists");
            INSTANCES.add(connection);
        }
    }
}
