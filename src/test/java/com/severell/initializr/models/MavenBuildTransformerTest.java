package com.severell.initializr.models;

public class MavenBuildTransformerTest implements BuildTransformerTest<MavenBuildTransformer>{
    @Override
    public MavenBuildTransformer transformer() {
        return new MavenBuildTransformer();
    }

}
