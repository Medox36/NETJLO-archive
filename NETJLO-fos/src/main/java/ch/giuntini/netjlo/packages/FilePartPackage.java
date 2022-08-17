package ch.giuntini.netjlo.packages;

import java.io.Serializable;

public class FilePartPackage implements Serializable {
    public final String information;
    public final boolean EOF;

    public FilePartPackage(String information) {
        this.information = information;
        EOF = false;
    }

    public FilePartPackage(boolean EOF) {
        this.EOF = EOF;
        information = null;
    }
}
