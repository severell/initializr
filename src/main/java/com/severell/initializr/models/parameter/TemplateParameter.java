package com.severell.initializr.models.parameter;

import com.severell.core.config.Config;

public class TemplateParameter implements Parameter{

    @Override
    public String getName() {
        return Config.get("NAME", "severell");
    }

    @Override
    public String getVersion() {
        return Config.get("VERSION", "0.0.1-SNAPSHOT");
    }

    @Override
    public String getArtifactId() {
        return Config.get("ARTIFACT_ID", "##test##");
    }

    @Override
    public String getGroupId() {
        return Config.get("GROUP_ID", "###tester###");
    }

    @Override
    public String getDescription() {
        return Config.get("DESCRIPTION", "__description__");
    }

    public String getSeverellName() {
        return Config.get("SEVERELL_NAME", "severell");
    }

    public String getArcheTypeVersion(){
        return Config.get("ARCHETYPE_VERSION");
    }

    public String getArcheTypeGroupId(){
        return Config.get("ARCHETYPE_GROUP_ID");
    }

    public String getArcheTypeArtifactId(){
        return Config.get("ARCHETYPE_ARTIFACT_ID");
    }

    public String getArcheTypeCatalog(){
        return Config.get("ARCHETYPE_CATALOG_ID");
    }

}
