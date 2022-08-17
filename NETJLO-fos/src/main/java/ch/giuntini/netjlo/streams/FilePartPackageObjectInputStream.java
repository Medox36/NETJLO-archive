package ch.giuntini.netjlo.streams;

import ch.giuntini.netjlo.packages.FilePartPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class FilePartPackageObjectInputStream extends ObjectInputStream {

    public FilePartPackageObjectInputStream(InputStream in) throws IOException {
        super(in);
        setObjectInputFilter(filterInfo -> {
            if (filterInfo.serialClass().equals(FilePartPackage.class))
                return ObjectInputFilter.Status.ALLOWED;
            else
                return ObjectInputFilter.Status.REJECTED;
        });
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass desc = super.readClassDescriptor();
        if (desc.getName().equals(FilePartPackage.class.getName()))
            return ObjectStreamClass.lookup(FilePartPackage.class);
        return desc;
    }
}
