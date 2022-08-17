package ch.giuntini.netjlo.threads;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.FilePartPackage;
import ch.giuntini.netjlo.streams.FilePartPackageObjectInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReceiverThread<I extends Interpretable> extends Thread implements AutoCloseable {

    private final String PATH;
    private final Socket socket;
    private final FilePartPackageObjectInputStream ois;
    private final I interpreter;
    private boolean stop;

    public FileReceiverThread(Socket socket, String rootPathForFiles, Class<I> interpreterC) {
        super("Receiving-Thread");
        this.socket = socket;
        this.PATH = rootPathForFiles;
        try {
            ois = new FilePartPackageObjectInputStream(new ObjectInputStream(new BufferedInputStream(socket.getInputStream())));
            interpreter = interpreterC.getConstructor().newInstance();
        } catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!stop) {
            cycle();
            Thread.onSpinWait();
        }
        try {
            socket.shutdownInput();
            //TODO check if stream isn't already closed through the shutdownInput() method
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cycle() {
        try {
            final String filename = ((FilePartPackage)ois.readObject()).information;
            Path target = Path.of(PATH + filename);
            File file = Files.createFile(target).toFile();
            BufferedWriter bw = Files.newBufferedWriter(target);
            FilePartPackage partPackage;
            while (!(partPackage = ((FilePartPackage)ois.readObject())).EOF) {
                bw.write(partPackage.information);
            }
            bw.flush();
            bw.close();
            interpreter.interpret(file);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void close() {
        stop = true;
    }
}
