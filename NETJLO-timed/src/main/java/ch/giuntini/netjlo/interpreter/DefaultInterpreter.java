package ch.giuntini.netjlo.interpreter;

import ch.giuntini.netjlo.packages.timed.TimedPackage;

public class DefaultInterpreter implements Interpretable<TimedPackage> {


    @Override
    public void interpret(TimedPackage p) {
        System.out.println("interpret at: " + p.timeStamp + "; information: " + p.information);
    }
}
