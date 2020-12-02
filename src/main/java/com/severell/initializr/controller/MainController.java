package com.severell.initializr.controller;

import com.severell.core.config.Config;
import com.severell.core.exceptions.ViewException;
import com.severell.core.http.Request;
import com.severell.core.http.Response;
import com.severell.initializr.internal.maven.MavenProjectGenerator;
import com.severell.initializr.internal.zip.Zipper;
import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.invoker.*;
import org.eclipse.jetty.io.ByteArrayEndPoint;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainController {

    public void index(Request request, Response resp) throws IOException, ViewException {
        resp.render("index.mustache", new HashMap<String, Object>());
    }

    public void generate(Request request, Response resp) throws IOException, ViewException, MavenInvocationException {
        System.setProperty("maven.home", Config.get("MAVEN_HOME"));
        Path file = Files.createTempDirectory("temp");

        MavenProjectGenerator projectGenerator =
                new MavenProjectGenerator(request.input("group"), request.input("artifact"));
        projectGenerator.generate(file);

        Zipper.zipDir(Path.of(file.toString(), request.input("artifact")));
        resp.file(new File(Path.of(file.toString(), request.input("artifact")) + ".zip"), "application/zip", "severell.zip");
    }

    public void generateOurselves(Request request, Response resp) throws IOException, ViewException, MavenInvocationException {
        //TODO Implement Code to create project and send for download


    }

}
