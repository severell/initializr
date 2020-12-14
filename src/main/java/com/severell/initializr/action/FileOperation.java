package com.severell.initializr.action;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

public abstract class FileOperation {
    private static final String fileSeparator = File.separator;
    private static final String archiveExtension = ".zip";


    protected abstract void cleanUp();

    public String getFileSeparator(){
        return fileSeparator;
    }

    public Path zip(Path sourcePath, String name) throws ExecutionException, InterruptedException {
        String zippedPath = String.valueOf(sourcePath).concat(fileSeparator).concat(name).concat(archiveExtension);
        Path path = Paths.get(zippedPath);
        zipDirectory(sourcePath, path);
        return path;
    }

    private void zipDirectory(Path rawDir, Path zipDir) throws ExecutionException, InterruptedException {
        ParallelScatterZipCreator scatterZip = new ParallelScatterZipCreator();
        try(FileOutputStream outputStream = new FileOutputStream(zipDir.toFile())) {
            try(ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(outputStream)) {
                zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);
                Files.walk(rawDir).parallel().filter(path -> path.toFile().isFile()).forEach(path -> {
                    InputStreamSupplier streamSupplier = () -> {
                        InputStream pathStream = null;
                        try {
                            pathStream = Files.newInputStream(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return pathStream;
                    };
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(String.valueOf(rawDir.relativize(path)));
                    zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                    scatterZip.addArchiveEntry(zipArchiveEntry, streamSupplier);
                });
                scatterZip.writeTo(zipOutputStream);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
