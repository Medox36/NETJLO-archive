package ch.giuntini.netjlo.streams;

import ch.giuntini.netjlo.packages.DefaultPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class DefaultPackageObjectInputStream extends ObjectInputStream {

    public DefaultPackageObjectInputStream(InputStream in) throws IOException {
        super(in);
        setObjectInputFilter(filterInfo -> {
            if (filterInfo.serialClass().equals(DefaultPackage.class))
                return ObjectInputFilter.Status.ALLOWED;
            else
                return ObjectInputFilter.Status.REJECTED;
        });
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass desc = super.readClassDescriptor();
        if (desc.getName().equals(DefaultPackage.class.getName())) {
            return ObjectStreamClass.lookup(DefaultPackage.class);
        }
        return desc;
    }
}
