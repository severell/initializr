package com.severell.initializr.controller;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.initializr.action.GeneratorException;
import com.severell.initializr.action.structure.StructureGenerator;
import com.severell.initializr.action.template.TemplateGenerator;
import com.severell.initializr.internal.maven.MavenProjectGenerator;
import com.severell.initializr.internal.zip.Zipper;
import com.severell.initializr.models.MavenBuildTransformer;
import com.severell.initializr.models.parameter.InputParameter;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MainController {

    public void index(Request request, Response resp) throws IOException, ViewException {
        resp.render("index.mustache", new HashMap<String, Object>());
    }

    public void generateOther(Request request, Response resp) throws IOException, ViewException, MavenInvocationException {
        System.setProperty("maven.home", Config.get("MAVEN_HOME"));
        Path file = Files.createTempDirectory("temp");

        MavenProjectGenerator projectGenerator =
                new MavenProjectGenerator(request.input("group"), request.input("artifact"));
        projectGenerator.generate(file);

        Zipper.zipDir(Path.of(file.toString(), request.input("artifact")));
        resp.download(new File(Path.of(file.toString(), request.input("artifact")) + ".zip"), "application/zip", "severell.zip");
    }

    public void generate(Request request, Response resp, TemplateGenerator templateGenerator) throws IOException, GeneratorException, ExecutionException, InterruptedException {
        InputParameter parameter = new InputParameter(request);
        StructureGenerator structureGenerator = null;
        try {
            structureGenerator = new StructureGenerator(parameter, new MavenBuildTransformer(), templateGenerator.getDirectory());
            structureGenerator.generate();
            Path downloadPath = structureGenerator.download();
            resp.download(downloadPath.toFile(), "application/zip", parameter.getName().concat(".zip"));
        }finally {
            if(Objects.nonNull(structureGenerator)) {
                structureGenerator.cleanUp();
            }
        }
    }

}
