package ch.giuntini.netjlo.sockets;

import ch.giuntini.netjlo.packages.timed.TimedPackage;

import java.util.Date;

public interface TimedSend<P extends TimedPackage> extends Send<P> {

    void send(P pack, int delay);

    void send(P pack, Date timestamp);
}
