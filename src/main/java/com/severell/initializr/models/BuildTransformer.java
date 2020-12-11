package com.severell.initializr.models;

import java.io.File;
import java.util.List;

public interface BuildTransformer {

    List<Permission> nameAccess();

    List<Permission> artifactAccess();

    List<Permission> groupAccess();

    List<Permission> versionAccess();

    List<Permission> dependencyAccess();

    String getBuildConfigPath();
}
