package com.severell.initializr.models.parameter;

import com.severell.core.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateParameterTest {

    @Test
    @DisplayName("Test default property without loaded config")
    public void testDefaultProperty(){
        TemplateParameter parameter = new TemplateParameter();
        assertFalse(Config.isLoaded());
        assertEquals(parameter.getName(), "severell");
        assertEquals(parameter.getVersion(), "0.0.1");
        assertEquals(parameter.getArtifactId(), "##test##");
        assertEquals(parameter.getGroupId(), "___tester___");
        assertEquals(parameter.getDescription(), "__description__");
    }

    @Test
    public void testEnvProperty() throws Exception {
        Config.setDir("src/test/resources");
        Config.loadConfig();
        TemplateParameter parameter = new TemplateParameter();
        assertTrue(Config.isLoaded());
        assertEquals(parameter.getSeverellName(), "severell");
        assertEquals(parameter.getArcheTypeVersion(), "0.0.1-SNAPSHOT");
        assertEquals(parameter.getArcheTypeGroupId(), "com.severell");
        assertEquals(parameter.getArcheTypeArtifactId(), "severell-archetype");
        Config.unload();
    }
}
