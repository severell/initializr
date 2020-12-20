package com.severell.initializr.action;

import com.severell.initializr.action.structure.StructureGenerator;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger LOG = LoggerFactory.getLogger(FileOperation.class);
    private static final String fileSeparator = File.separator;
    private static final String archiveExtension = ".zip";


    protected abstract void cleanUp();

    public String getFileSeparator(){
        return fileSeparator;
    }

    protected Path zip(Path sourcePath, String name) throws ExecutionException, InterruptedException {
        String zippedPath = String.valueOf(sourcePath).concat(fileSeparator).concat(name).concat(archiveExtension);
        Path path = Paths.get(zippedPath);
        zipDirectory(sourcePath, path, name);
        return path;
    }

    private void zipDirectory(Path rawDir, Path zipDir, String name) throws ExecutionException, InterruptedException {
        ParallelScatterZipCreator scatterZip = new ParallelScatterZipCreator();
        try(FileOutputStream outputStream = new FileOutputStream(zipDir.toFile())) {
            try(ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(outputStream)) {
                zipOutputStream.setUseZip64(Zip64Mode.AsNeeded);
                Files.walk(rawDir).parallel().filter(path -> path.toFile().isFile() && !path.equals(zipDir)).forEach(path -> {
                    InputStreamSupplier streamSupplier = () -> {
                        InputStream pathStream = null;
                        try {
                            pathStream = Files.newInputStream(path);
                        } catch (IOException e) {
                            LOG.error("IO error has occurred", e);
                        }
                        return pathStream;
                    };
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(name.concat(fileSeparator).concat( String.valueOf(rawDir.relativize(path))));
                    zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                    scatterZip.addArchiveEntry(zipArchiveEntry, streamSupplier);
                });
                scatterZip.writeTo(zipOutputStream);
            }
        }catch (IOException ex){
            LOG.error("IO error has occurred", ex);
        }
    }
}
