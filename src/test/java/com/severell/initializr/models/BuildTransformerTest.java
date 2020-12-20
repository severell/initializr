package com.severell.initializr.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public interface BuildTransformerTest <T extends BuildTransformer>{
    T transformer();

    @Test
    default void testMethod(){
        assertNotNull(transformer().nameAccess());
        assertFalse(transformer().artifactAccess().isEmpty());
        assertFalse(transformer().groupAccess().isEmpty());
        assertFalse(transformer().dependencyAccess().isEmpty());
        assertFalse(transformer().versionAccess().isEmpty());
        assertNotNull(transformer().descriptionAccess());
        assertFalse(transformer().getBuildConfigPath().isEmpty());
    }
}
