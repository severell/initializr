package com.severell.initializr.action.structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StructureDirectoryCopy<Source, Dest>{
    private final static Logger LOG = LoggerFactory.getLogger(StructureDirectoryCopy.class);
    private BiFunction<Source, Dest, Boolean> callback;

    StructureDirectoryCopy(){
        this.callback = this::apply;
    }


    private Boolean apply(Source source, Dest dest) {
        Path paths[] = convertToPath(source, dest);
        return this.copyFolder(paths[0], paths[1]);
    }

    private boolean copyFolder(Path source, Path dest)  {
        boolean status = false;
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> copy(path, dest.resolve(source.relativize(path))));
            status = true;
        } catch (IOException e) {
            LOG.error("IO error has occurred", e);
        }
        return status;
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private Path[] convertToPath(Source source, Dest dest){
        return new Path[]{switcher(source), switcher(dest)};
    }

    private Path switcher(Object object){
        Path path = null;
        if (object instanceof Path){
            path = (Path)object;
        }else if (object instanceof File){
            path = ((File)object).toPath();
        }else if (object instanceof String){
            path = Paths.get((String) object);
        }
        return path;
    }

    boolean call(Source source, Dest dest){
        return callback.apply(source, dest);
    }
}
