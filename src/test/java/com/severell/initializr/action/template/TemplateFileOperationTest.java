package com.severell.initializr.action.template;

import com.severell.initializr.models.parameter.TemplateParameter;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateFileOperationTest {

    @Test
    public void testTemplatePaths(){
        TemplateParameter parameter = new TemplateParameter();
        TemplateFileOperation operation = new TemplateFileOperation(parameter, "0.0.1");

        String templateDir = Files.temporaryFolderPath().concat( File.separator).concat(parameter.getName());;
        assertEquals(operation.getDirectoryPath(), Paths.get(templateDir.concat(File.separator).concat(parameter.getArtifactId())));
        assertTrue(new File(templateDir).exists());
        operation.cleanUp();
        assertFalse(new File(templateDir).exists());
    }
}
