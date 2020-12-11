package com.severell.initializr.action.structure;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class StructureDirectoryMove<Source, Dest> {
    private BiFunction<Source, Dest, Boolean> callback;
    private String mainDir, separator;

    StructureDirectoryMove(String separator){
        this.separator = separator;
        this.callback = this::apply;
    }

    private Boolean apply(Source source, Dest dest) {
        String paths[] = convertToPath(source, dest);
        return this.moveFolder(paths[0], paths[1]);
    }

    private boolean moveFolder(String source, String dest)  {
        boolean status = true;
        Path newPath = Paths.get(mainDir.concat(separator).concat(dest.replace(".", separator)));
        try (Stream<Path> paths = Files.walk(Paths.get(mainDir))) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> {
                        String currentPath = Paths.get(mainDir).relativize(path).toString().replace(separator, ".");
                        if(currentPath.equals(source)){
                            try {
                                FileUtils.copyDirectory(path.toFile(), newPath.toFile());
                                FileUtils.deleteDirectory(path.toFile());
                                if(path.compareTo(newPath) > 0){
                                    Path pathToDelete = newPath.resolveSibling(source.split(separator)[0]);
                                    FileUtils.deleteDirectory(pathToDelete.toFile());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }



    private String[] convertToPath(Source source, Dest dest){
        return new String[]{switcher(source), switcher(dest)};
    }

    private String switcher(Object object){
        String path = null;
        if (object instanceof Path){
            path = getRelativePackage((Path) object);
        }else if (object instanceof File){
            Path filePath = ((File)object).toPath();
            path = getRelativePackage(filePath);
        }else if (object instanceof String){
            path = (String) object;
        }
        return path;
    }

    boolean call(String mainDir, Source source, Dest dest){
        this.mainDir = mainDir;
        return callback.apply(source, dest);
    }

    private String getRelativePackage(Path path){
        Path relativePackage = Paths.get(mainDir).relativize(path);
        return String.valueOf(relativePackage).replace(separator, ".");
    }
}
