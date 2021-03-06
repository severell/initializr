package com.severell.initializr.models;

import java.util.List;

public class MavenBuildTransformer implements BuildTransformer {

    @Override
    public List<Permission> nameAccess() {
        return List.of();
    }

    @Override
    public List<Permission> artifactAccess() {
        return List.of(Permission.BUILD_CONFIG, Permission.JAVA, Permission.PACKAGE, Permission.SEVERELL_JSON);
    }

    @Override
    public List<Permission> groupAccess() {
        return List.of( Permission.BUILD_CONFIG, Permission.JAVA, Permission.PACKAGE, Permission.SEVERELL_JSON);
    }

    @Override
    public List<Permission> versionAccess() {
        return List.of(Permission.BUILD_CONFIG);
    }

    @Override
    public List<Permission> dependencyAccess() {
        return List.of(Permission.BUILD_CONFIG);
    }

    @Override
    public List<Permission> descriptionAccess() {
        return  List.of();
    }

    @Override
    public String getBuildConfigPath() {
        return "pom.xml";
    }


}
