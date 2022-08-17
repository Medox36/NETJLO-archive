package ch.giuntini.netjlo.sockets;

import ch.giuntini.netjlo.packages.DefaultPackage;

public interface Send<P extends DefaultPackage> {

    void send(P pack);
}
