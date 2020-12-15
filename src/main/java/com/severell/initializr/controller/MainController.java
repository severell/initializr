package com.severell.initializr.controller;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.initializr.action.template.TemplateGenerator;
import com.severell.initializr.internal.maven.MavenProjectGenerator;
import com.severell.initializr.internal.zip.Zipper;
import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.invoker.*;

import com.severell.initializr.action.GeneratorException;
import com.severell.initializr.action.structure.StructureGenerator;
import com.severell.initializr.models.MavenBuildTransformer;
import com.severell.initializr.models.parameter.InputParameter;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
        StructureGenerator generation = new StructureGenerator(parameter, new MavenBuildTransformer(), templateGenerator.getDirectory());
        generation.generate();
        resp.setContentType("application/zip");
        String headerValue = "attachment; filename=".concat(parameter.getName()).concat(".zip");
        resp.setHeader("Content-disposition",headerValue);
        ServletOutputStream outputStream = resp.getOutputStream();
        InputStream inputStream = generation.download();
        IOUtils.copy(inputStream, outputStream);
    }

}
