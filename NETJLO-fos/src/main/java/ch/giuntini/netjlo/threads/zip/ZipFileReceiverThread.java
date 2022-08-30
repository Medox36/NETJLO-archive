package ch.giuntini.netjlo.threads.zip;

import ch.giuntini.netjlo.interpreter.Interpretable;
import ch.giuntini.netjlo.packages.FilePartPackage;
import ch.giuntini.netjlo.streams.FilePartPackageObjectInputStream;
import ch.giuntini.netjlo.zip.ZipUtil;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZipFileReceiverThread<I extends Interpretable> extends Thread implements AutoCloseable {

    private final String PATH;
    private final Socket socket;
    private final FilePartPackageObjectInputStream ois;
    private final I interpreter;
    private final boolean unzipDirs;
    private boolean stop;

    public ZipFileReceiverThread(Socket socket, String rootPathForFiles, Class<I> interpreterC) {
        this(socket, rootPathForFiles, interpreterC, true);
    }

    public ZipFileReceiverThread(Socket socket, String rootPathForFiles, Class<I> interpreterC, boolean unzipDirs) {
        super("Receiving-Thread");
        this.socket = socket;
        this.PATH = rootPathForFiles;
        this.unzipDirs = unzipDirs;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cycle() {
        try {
            final String filename = ((FilePartPackage)ois.readObject()).information;
            final boolean wasDir = Boolean.parseBoolean(((FilePartPackage)ois.readObject()).information);
            Path target = Path.of(PATH + filename);
            File file = Files.createFile(target).toFile();
            BufferedWriter bw = Files.newBufferedWriter(target);
            FilePartPackage partPackage;
            while (!(partPackage = ((FilePartPackage)ois.readObject())).EOF) {
                bw.write(partPackage.information);
            }
            bw.flush();
            bw.close();
            if (unzipDirs && file.getName().endsWith(".zip")) {
                if (wasDir) {
                    file = ZipUtil.unzipDirAndSubDirs(file, PATH);
                } else {
                    file = ZipUtil.unzipSingleFile(file, PATH);
                }
            }
            interpreter.interpret(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public synchronized void close() {
        stop = true;
    }
}