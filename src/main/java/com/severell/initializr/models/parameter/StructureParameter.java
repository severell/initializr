package com.severell.initializr.models.parameter;

import java.nio.file.Path;

public class StructureParameter implements Parameter{
    private String name, version, artifactId, groupId;

    public StructureParameter(String name, String version, String artifactId, String groupId){
        this.name = name;
        this.version = version;
        this.artifactId = artifactId;
        this.groupId = groupId;
    }
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }


}
