package com.severell.initializr.action.structure;

import com.severell.initializr.action.FileOperation;
import com.severell.initializr.models.parameter.InputParameter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureFileOperation extends FileOperation {
    private StructureDirectoryCopy<Path, Path> directoryCopy;
    private StructureDirectoryMove<String, String> directoryMove;
    private StructureMapperResolver resolver;
    private InputParameter parameter;

    StructureFileOperation(InputParameter parameter){
        this.parameter = parameter;
        directoryCopy = new StructureDirectoryCopy<>();
        directoryMove = new StructureDirectoryMove<>(this.getFileSeparator());
        resolver = new StructureMapperResolver();
    }

    private Path getStructureDirectoryPath(){
        String pathDir = System.getProperty("java.io.tmpdir") + this.getFileSeparator() + parameter.getName() + parameter.getSessionHash() ;
        Path path = Paths.get(pathDir);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private String getMain(){
        return File.separator.concat("src").concat(File.separator).concat("main");
    }

    String getDBDirPath(){
        return File.separator.concat("src").concat(File.separator).concat("db");
    }

    String getJavaDirPath(){
        return getMain().concat(File.separator).concat("java");
    }

    String getSeverellConfigPath(){
        return getResourceDirPath().concat(File.separator).concat(".env");
    }

    String getResourceDirPath(){
        return  getMain().concat(File.separator).concat("resources");
    }

    String copyTemplateToLocal(Path sourcePath){
        Path destPath = getStructureDirectoryPath();
        boolean status = directoryCopy.call(sourcePath, destPath);
        return status ? destPath.toAbsolutePath().toString() : "";
    }

    void modifyStructureFile(String path, String value, String replacee){
        List<StructureMapper> mappers = resolver.getFileMap(path, value);
        if(mappers != null && mappers.size() > 0) {
            boolean inBoundStrategy = resolver.getReplacement(mappers.get(0).getContent().toString(), value, replacee).length()
                    < (mappers.get(0).getLength() - resolver.getOffset());
            if(inBoundStrategy) {
                mappers.forEach(mapper -> {
                    String replacement = resolver.getReplacement(mapper.getContent().toString(), value, replacee);
                    mapper.setContent(replacement);
                    mapper.setFilename(path);
                    resolver.modifyInFile(mapper);
                });
            }else{
                StructureMapper mapper = mappers.get(0);
                mapper.setFilename(path);
                String fileContent = resolver.getContent(mappers.get(0).getFilename(), mappers.get(0).getStart());
                for (StructureMapper m : mappers) {
                    String line = m.getContent().toString();
                    String newReplacement = resolver.getReplacement(line, value, replacee) + resolver.getSeparator();
                    fileContent = fileContent.replace(line, newReplacement);
                }
                mapper.setContent(fileContent);
                mapper.setLength(fileContent.length());
                resolver.modifyOutFile(mapper);
            }
        }
    }


    List<Path> getAllFilePaths(String origin){
        List<Path> paths = null;
        Path originPath = Paths.get(origin);
        try (Stream<Path> stream = Files.walk(originPath)) {
            paths = stream.filter(path -> path.toFile().isFile()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    Boolean renameDirectoryPackage(String startingDirectory, String oldValue, String newValue){
        return directoryMove.call(startingDirectory, oldValue, newValue);
    }

    Path getZippedDirectory(String name) throws ExecutionException, InterruptedException {
        return this.zip(getStructureDirectoryPath(), name);
    }

    protected void cleanUp(){
        File file = getStructureDirectoryPath().toFile();
        if(file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
