package com.severell.initializr.action.template;

import com.severell.initializr.action.FileOperation;
import com.severell.initializr.models.parameter.TemplateParameter;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Function;

public class TemplateFileOperation extends FileOperation {
    private TemplateParameter parameter ;
    private String dirPath;

    TemplateFileOperation(TemplateParameter param){
        parameter = param;
        dirPath = System.getProperty("java.io.tmpdir").concat( this.getFileSeparator()).concat(parameter.getName());
    }

    private Path getProjectDirectoryPath(){
        String path = System.getProperty("java.io.tmpdir").concat( this.getFileSeparator()).concat(parameter.getSeverellName());
        return Paths.get(path);
    }

    private Path getTemplatePath(){
        Path path = Paths.get(dirPath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    Path getDirectoryPath(){
        String dirPath =  getTemplatePath().toAbsolutePath().toString().concat(this.getFileSeparator()).concat(parameter.getArtifactId());
        return Paths.get(dirPath);
    }

    private String getDirectoryAbsolutePath(){
        return getTemplatePath().toAbsolutePath().toString();
    }

    private Properties getTemplateProperties(){
        Properties properties = new Properties();
        properties.setProperty("groupId", parameter.getGroupId());
        properties.setProperty("artifactId", parameter.getArtifactId());
        properties.setProperty("version", parameter.getVersion());
        properties.setProperty("archetypeVersion", parameter.getArcheTypeVersion());
        properties.setProperty("archetypeGroupId", parameter.getArcheTypeGroupId());
        properties.setProperty("archetypeArtifactId", parameter.getArcheTypeArtifactId());
        properties.setProperty("archetypeVersion", parameter.getArcheTypeVersion());
        properties.setProperty("archetypeCatalog", parameter.getArcheTypeCatalog());
        return properties;
    }

    private InvocationRequest getPreLoader(){
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( getProjectDirectoryPath().toAbsolutePath().toString().concat(getFileSeparator()).concat("pom.xml") ) );
        request.setGoals(Arrays.asList("clean", "install"));
        request.setProperties(getTemplateProperties());
        return request;
    }

    private InvocationRequest getLoader(){
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Collections.singletonList("archetype:generate"));
        request.setProperties(getTemplateProperties());
        return request;
    }

    private boolean getInvoker(InvocationRequest request){
        boolean status = false;
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
        Path dir = getTemplatePath();
        try {
            deleteDir(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        invoker.setWorkingDirectory(new File(getDirectoryAbsolutePath()));
        InvocationResult result = null;
        try {
            result = invoker.execute( request );
            if ( result.getExitCode() == 0 ){
                status = true;
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean generate(){
        Function<InvocationRequest, Boolean> generateTemplate = this::getInvoker;
        InvocationRequest preLoader = getPreLoader();
        boolean status =  generateTemplate.apply(preLoader);
        return status ? generateTemplate.apply(getLoader()) : status;
    }

    private void deleteDir(Path folder) throws IOException {
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    protected void cleanUp() {
        File file = Paths.get(dirPath).toFile();
        if(file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
