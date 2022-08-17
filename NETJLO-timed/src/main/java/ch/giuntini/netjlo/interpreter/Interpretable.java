package ch.giuntini.netjlo.interpreter;

import ch.giuntini.netjlo.packages.DefaultPackage;

public interface Interpretable<T extends DefaultPackage> {

    void interpret(T p);
}
