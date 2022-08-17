package ch.giuntini.netjlo.interpreter;

import ch.giuntini.netjlo.packages.DefaultPackage;

public class DefaultInterpreter implements Interpretable<DefaultPackage> {

    @Override
    public void interpret(DefaultPackage p) {
        System.out.println(p.information);
    }
}
