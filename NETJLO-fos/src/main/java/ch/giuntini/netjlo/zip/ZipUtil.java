package ch.giuntini.netjlo.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
     *
     * @param file  file to zip
     * @return zipped file
     * @throws IOException if an I/O error occurs
     */
    public static File zipSingleFile(File file) throws IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file can't be a Directory when calling this specific method");
        }
        Path path = Path.of(file.getAbsolutePath());
        Path zipName = Path.of(path + ".zip");
        File zipFile = zipName.toFile();
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile.getName()));
        zipOut.putNextEntry(new ZipEntry(file.getName()));
        Files.copy(path, zipOut);
        zipOut.flush();
        zipOut.closeEntry();
        zipOut.close();
        return zipFile;
    }

    /**
     *
     * @param file file to unzip
     * @param path where to unzip
     * @return unzipped file
     * @throws IOException if an I/O error occurs
     */
    public static File unzipSingleFile(File file, String path) throws IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file can't be a Directory when calling this specific method");
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
        Path destFilePath = Files.createFile(Path.of(path + file.getName().replace(".zip", "")));
        zipIn.getNextEntry();
        Files.copy(zipIn, destFilePath);
        zipIn.closeEntry();
        zipIn.close();
        return destFilePath.toFile();
    }

    /**
     *
     * @param file directory to zip
     * @return zipped directory (*.zip)
     * @throws IOException if an I/O error occurs
     */
    public static File zipDirAndSubDirs(File file) throws IOException {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("The file must be a Directory when calling this specific method");
        }
        Path path = Path.of(file.getAbsolutePath());
        Path zipName = Path.of(path + ".zip");
        File zipDir = zipName.toFile();
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipDir));
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zipOut.putNextEntry(new ZipEntry(path.relativize(file).toString()));
                Files.copy(file, zipOut);
                zipOut.flush();
                zipOut.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
        zipOut.close();
        return zipDir;
    }

    /**
     *
     * @param file file to unzip
     * @param path where to unzip
     * @return unzipped directory
     * @throws IOException if an I/O error occurs
     */
    public static File unzipDirAndSubDirs(File file, String path) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
        Path destDirPath = Files.createDirectory(Path.of(path + file.getName().replace(".zip", "")));
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null) {
            File newFile = newFile(destDirPath.toFile(), zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
                break;
            }
            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
            }
            Files.copy(zipIn, newFile.toPath());
        }
        zipIn.closeEntry();
        zipIn.close();
        return destDirPath.toFile();
    }

    /**
     *
     * @param name name of the .zip file
     * @param path path where to store
     * @param files files to zip
     * @return zipped files
     * @throws IOException if an I/O error occurs
     */
    @Deprecated
    public static File zipMultipleDirsAndSubDirs(String name, String path, File... files) throws IOException {
        Path aPath = Path.of(path, name).toAbsolutePath();
        Path zipName = Path.of(path + ".zip");
        File zipDir = zipName.toFile();
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipDir));
        for (File file : files) {
            if (file.isDirectory()) {
                Files.walkFileTree(Path.of(aPath + File.separator + file.getName()), new SimpleFileVisitor<>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        zipOut.putNextEntry(new ZipEntry(aPath.relativize(file).toString()));
                        Files.copy(file, zipOut);
                        zipOut.flush();
                        zipOut.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(file.toPath(), zipOut);
                zipOut.flush();
                zipOut.closeEntry();
            }
        }
        zipOut.close();
        return zipDir;
    }

    /**
     *
     * @param file file to unzip
     * @param path where to unzip
     * @return unzipped directory to wich all files were unzipped
     * @throws IOException throws IOException
     */
    @Deprecated
    public static File unzipMultipleDirsAndSubDirs(File file, String path) throws IOException {
        //TODO implement
        return unzipDirAndSubDirs(file, path);
    }

    /**
     * <p>
     * creates a new file in the unzipping process
     * <p>
     * This method guards against writing files to the file system outside the target folder.
     * This vulnerability is called <a href="https://snyk.io/research/zip-slip-vulnerability">Zip Slip</a>
     *
     * @param destinationDir the destination directory for the file/directory in the zipEntry
     * @param zipEntry of which a new file must be made
     * @return the new file
     * @throws IOException if zip slipping occurred
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
