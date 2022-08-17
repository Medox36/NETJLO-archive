package ch.giuntini.netjlo.packages;

import java.io.Serializable;

public class DefaultPackage implements Serializable {
    public final String information;

    public DefaultPackage(String information) {
        this.information = information;
    }
}
