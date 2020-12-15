package com.severell.initializr.action.structure;

import com.severell.initializr.models.BuildTransformer;
import com.severell.initializr.models.Permission;
import com.severell.initializr.models.parameter.InputParameter;
import com.severell.initializr.models.parameter.Parameter;
import com.severell.initializr.models.parameter.TemplateParameter;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StructureGenerator {
    private BuildTransformer transformer;
    private StructureFileOperation fileOperation;
    private InputParameter inputParameter;
    private Parameter templateParam = new TemplateParameter();
    private Path sourcePath;
    private Path targetPath;
    //argument for move/rename utility
    private Boolean useParent = false;


    public StructureGenerator(InputParameter param, BuildTransformer transformer, Path source){
        this.transformer = transformer;
        sourcePath = source;
        this.inputParameter = param;
        this.fileOperation = new StructureFileOperation(param);
    }

    public Path getDirectory(){
        return targetPath;
    }

    public void generate(){
        String structurePath = fileOperation.copyTemplateToLocal(sourcePath);
        if(StringUtils.isNotEmpty(structurePath)){
            targetPath = Paths.get(structurePath);
            try {
                nameHandler();
                groupHandler();
                artifactHandler();
                versionHandler();
            }catch (Exception ex){
                ex.getStackTrace();
            }
        }
        //TODO handle error, throw exception
    }

    public Path download() throws ExecutionException, InterruptedException {
        Path path;
        String name = inputParameter.getName();
        path = fileOperation.getZippedDirectory(name);
        return path;
    }

    private void nameHandler(){
        List<Permission> permission = transformer.nameAccess();
        String value = templateParam.getName();
        String newValue = inputParameter.getName();
        handler(permission, value, newValue);
    }

    private void artifactHandler(){
        List<Permission> permission = transformer.artifactAccess();
        String value = templateParam.getArtifactId();
        String newValue = inputParameter.getArtifactId();
        useParent = true;
        handler(permission, value, newValue);
    }
    private void groupHandler(){
        List<Permission> permission = transformer.groupAccess();
        String value = templateParam.getGroupId();
        String newValue = inputParameter.getGroupId();
        useParent = false;
        handler(permission, value, newValue);
    }
    private void versionHandler(){
        List<Permission> permission = transformer.versionAccess();
        String value = templateParam.getVersion();
        String newValue = inputParameter.getVersion();
        handler(permission, value, newValue);
    }

    private void handler(List<Permission> transformerPermission, String templateName, String structureName){
        transformerPermission.forEach(permission -> {
            switch (permission){
                case BUILD_CONFIG:{
                    String configPath =  getDirectory().toString() + fileOperation.getFileSeparator() + transformer.getBuildConfigPath();
                    fileOperation.modifyStructureFile(configPath, templateName, structureName);
                    break;
                }
                case JAVA:{
                    String javaDir = getDirectory().toString() + fileOperation.getJavaDirPath();
                    List<Path> paths = fileOperation.getAllFilePaths(javaDir);
                    paths.forEach(path -> fileOperation.modifyStructureFile(path.toAbsolutePath().toString(), templateName, structureName));
                    break;
                }
                case RESOURCE:{
                    String resourceDir = getDirectory().toString()  + fileOperation.getResourceDirPath();
                    List<Path> paths = fileOperation.getAllFilePaths(resourceDir);
                    paths.forEach(path -> fileOperation.modifyStructureFile(path.toAbsolutePath().toString(), templateName, structureName));
                    break;
                }
                case SEVERELL_CONFIG:{
                    String configPath = getDirectory().toString()  + fileOperation.getSeverellConfigPath();
                    fileOperation.modifyStructureFile(configPath, templateName, structureName);
                    break;
                }
                case DB:{
                    String resourceDir = getDirectory().toString()  + fileOperation.getDBDirPath();
                    List<Path> paths = fileOperation.getAllFilePaths(resourceDir);
                    paths.forEach(path -> fileOperation.modifyStructureFile(path.toAbsolutePath().toString(), templateName, structureName));
                    break;
                }
                case UNIQUE_FILE:{
                    String resourceDir = getDirectory().toString();
                    List<Path> paths = fileOperation.getAllFilePaths(resourceDir);
                    paths.forEach(path -> fileOperation.modifyStructureFile(path.toAbsolutePath().toString(), templateName, structureName));
                    break;
                }
                case PACKAGE:{
                    String javaDir = getDirectory().toString() + fileOperation.getJavaDirPath();
                    fileOperation.renameDirectoryPackage(javaDir, templateName, structureName, useParent);
                    break;
                }
                default:
                    break;
            }
        });
    }

    public void cleanUp(){
        fileOperation.cleanUp();
    }
}
