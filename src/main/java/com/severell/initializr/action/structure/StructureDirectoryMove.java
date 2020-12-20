package com.severell.initializr.action.structure;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureDirectoryMove<Source, Dest> {
    private final static Logger LOG = LoggerFactory.getLogger(StructureDirectoryMove.class);

    private BiFunction<Source, Dest, Boolean> callback;
    private String mainDir, separator;
    //control util
    private Boolean useParent = false;
    private Boolean stopper = false;

    StructureDirectoryMove(String separator){
        this.separator = separator;
        this.callback = this::apply;
    }

    public Boolean getUseParent() {
        return useParent;
    }

    public void setUseParent(Boolean useParent) {
        this.stopper = false;
        this.useParent = useParent;
    }

    private Boolean apply(Source source, Dest dest) {
        String paths[] = convertToPath(source, dest);
        return this.moveFolder(paths[0], paths[1]);
    }

    private boolean moveFolder(String source, String dest)  {
        boolean status = true;
        try (Stream<Path> paths = Files.walk(Paths.get(mainDir))) {
            paths.filter(Files::isDirectory).forEach(path -> {
                String currentPath = Paths.get(mainDir).relativize(path).toString().replace(separator, ".");
                if(currentPath.contains(source) && !stopper){
                    Path newPath;
                    try {
                        newPath = resolvePath(currentPath, dest);
                        FileUtils.copyDirectory(path.toFile(), newPath.toFile());
                        FileUtils.deleteDirectory(path.toFile());
                        if(path.compareTo(newPath) > 0){
                            String[] pathBranch = source.split("\\.");
                            String pathTree = Arrays.stream(pathBranch).collect(Collectors.joining(separator));
                            Path pathToDelete = newPath.resolveSibling(pathTree);
                            if(Files.exists(pathToDelete)) {
                                FileUtils.deleteDirectory(pathToDelete.toFile());
                            }
                        }
                        stopper = true;
                    } catch (IOException e) {
                        LOG.error("IO error has occurred", e);
                    }

                }
            });
        } catch (IOException e) {
            LOG.error("IO error has occurred", e);
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

    private Path resolvePath(String currentPath, String destPath){
        Path newPath;
        if(useParent){
            String[] list = currentPath.split("\\.");
            int siblingOffset = list.length - 1;
            String pathTree = String.join(".", Arrays.asList(list).subList(0, siblingOffset));
            String newDest = pathTree.concat(".").concat(destPath);
            newPath = Paths.get(mainDir.concat(separator).concat(newDest.replace(".", separator)));
        }else {
            newPath = Paths.get(mainDir.concat(separator).concat(destPath.replace(".", separator)));
        }
        return newPath;
    }
}
