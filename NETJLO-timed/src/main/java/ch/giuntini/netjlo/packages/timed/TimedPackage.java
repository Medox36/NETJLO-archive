package ch.giuntini.netjlo.packages.timed;

import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.Serializable;
import java.util.Date;

public class TimedPackage extends DefaultPackage implements Serializable {

    /**
     * the timestamp at which the package should be interpreted by the maschine the socket is connected to
     */
    public Date timeStamp;

    public TimedPackage(String information) {
        super(information);
    }
}