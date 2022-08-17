package ch.giuntini.netjlo.packages;

import java.io.Serializable;

public class Package extends DefaultPackage implements Serializable {
    public String serverPrefix;
    public String prefix;
    public String uuid;

    public Package(String serverPrefix, String prefix, String information, String uuid) {
        super(information);
        this.serverPrefix = serverPrefix;
        this.prefix = prefix;
        this.uuid = uuid;
    }
}
