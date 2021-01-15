package com.severell.initializr.action.template;

import com.severell.initializr.models.BuildTransformer;
import com.severell.initializr.models.parameter.TemplateParameter;

import java.nio.file.Path;

public class TemplateGenerator {
    private TemplateParameter parameter = new TemplateParameter();
    private TemplateFileOperation fileOperation = null;
    private  BuildTransformer transformer;


    public TemplateGenerator(TemplateParameter parameter, BuildTransformer buildTransformer, String version){
        this.transformer = buildTransformer;
        this.parameter = parameter;
        fileOperation = new TemplateFileOperation(parameter, version);
    }

    public Path getDirectory(){
        return fileOperation.getDirectoryPath();
    }

    public boolean generate(){
        return fileOperation.generate();
    }


}
