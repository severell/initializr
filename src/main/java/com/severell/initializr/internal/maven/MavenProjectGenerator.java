package com.severell.initializr.internal.maven;

import org.apache.maven.shared.invoker.*;

import java.nio.file.Path;
import java.util.Collections;

public class MavenProjectGenerator {

    private String group;
    private String artifact;

    public MavenProjectGenerator(String group, String artifact) {
        this.group = group;
        this.artifact = artifact;
    }

    public void generate(Path output) throws MavenInvocationException {
        InvocationRequest req = new DefaultInvocationRequest();
        req.setBatchMode(true);
        req.setGoals( Collections.singletonList( String.format("archetype:generate -B -DarchetypeGroupId=com.severell -DarchetypeArtifactId=severell-archetype -DgroupId=%s -DartifactId=%s -Dversion=1.0-SNAPSHOT", group, artifact )) );

        req.setBaseDirectory(output.toFile());


        Invoker invoker = new DefaultInvoker();
        invoker.execute( req );
    }
}
