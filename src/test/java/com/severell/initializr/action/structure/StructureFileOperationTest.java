package com.severell.initializr.action.structure;

import com.severell.initializr.models.parameter.InputParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StructureFileOperationTest {

    static @TempDir File parent;

    private static Stream<Arguments> provideSourceAndDestPath() throws IOException {
        Path source = new File(parent, "source").toPath();
        Path dest = new File(parent, "dest").toPath();
        if(!source.toFile().exists()) {
            Files.createDirectory(source);
        }
        if(!dest.toFile().exists()) {
            Files.createDirectory(dest);
        }
        return Stream.of(
                Arguments.of(source, dest)
        );
    }

    private static Stream<Arguments> provideRenameDirectoryPath() throws IOException {
        Path renamer = new File(parent, "renamer").toPath();
        if(!renamer.toFile().exists()) {
            Files.createDirectory(renamer);
        }
        return Stream.of(
                Arguments.of(renamer)
        );
    }

    private static Stream<Arguments> provideModifiedIndexAndLineIndex() throws IOException {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(2, 1),
                Arguments.of(4, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSourceAndDestPath")
    public void testCopyTemplateToLocal(Path source, Path dest) throws IOException {
        String filename = "source.txt";
        Path sourceText = source.resolve(filename);
        List<String> lines = Arrays.asList("severell", "tester");
        Files.write(sourceText, lines, StandardCharsets.UTF_8);

        InputParameter parameter = mock(InputParameter.class);
        StructureFileOperation operation = new StructureFileOperation(parameter);
        StructureFileOperation spyOperation = spy(operation);
        doReturn(dest).when(spyOperation).getStructureDirectoryPath();

        assertFalse(dest.resolve(filename).toFile().exists());
        Path expectedResult = spyOperation.copyTemplateToLocal(source);
        assertTrue(dest.resolve(filename).toFile().exists());
        assertEquals(expectedResult, dest);
        assertEquals(Files.readAllLines(expectedResult.resolve(filename)).size(), 2);
    }

    @Test
    public void testStructurePaths(){
        InputParameter parameter = mock(InputParameter.class);
        StructureFileOperation operation = new StructureFileOperation(parameter);
        String mainPath = File.separator.concat("src").concat(File.separator).concat("main");
        assertEquals(operation.getJavaDirPath(), mainPath.concat(File.separator).concat("java"));
        assertEquals(operation.getResourceDirPath(), mainPath.concat(File.separator).concat("resources"));
        assertEquals(operation.getSeverellConfigPath(), mainPath.concat(File.separator).concat("resources").concat(File.separator).concat(".env"));
        assertEquals(operation.getDBDirPath(), File.separator.concat("src").concat(File.separator).concat("db"));
    }

    @ParameterizedTest
    @MethodSource("provideModifiedIndexAndLineIndex")
    public void testModifyStructureFile(int modIndex, int lineIndex) throws IOException {
        String filename = "ModifiableSource.txt";
        Path sourceText = parent.toPath().resolve(filename);
        List<String> originalLines = Arrays.asList("<groupId>com.parent</groupId>", "<artifactId>sample</artifactId>", "<version>2020-SNAPSHOT</version>");
        List<String> modifiedLines = Arrays.asList("com.modified", "modifiedSample", "0.0.0");
        Path filePath = Files.write(sourceText, originalLines, StandardCharsets.UTF_8);

        InputParameter parameter = mock(InputParameter.class);
        StructureFileOperation operation = new StructureFileOperation(parameter);
        operation.modifyStructureFile(String.valueOf(filePath), originalLines.get(0), modifiedLines.get(0));
        operation.modifyStructureFile(String.valueOf(filePath), originalLines.get(1), modifiedLines.get(1));
        operation.modifyStructureFile(String.valueOf(filePath), originalLines.get(2), modifiedLines.get(2));
        List<String> modifiedResult = Files.readAllLines(filePath);
        assertTrue(modifiedResult.get(modIndex).contains(modifiedLines.get(lineIndex)));
    }

    @ParameterizedTest
    @MethodSource("provideRenameDirectoryPath")
    public void testRenameDirectoryPackage(Path source) throws IOException {
        String filename = "null.txt", renamedChild = "renamed";
        Path sourceText = source.resolve(filename);
        List<String> originalLines = Collections.singletonList("null content");
        Files.write(sourceText, originalLines, StandardCharsets.UTF_8);

        InputParameter parameter = mock(InputParameter.class);
        StructureFileOperation operation = new StructureFileOperation(parameter);
        boolean result = operation.renameDirectoryPackage(String.valueOf(parent), String.valueOf(parent.toPath().relativize(source)), renamedChild, true);
        assertTrue(result);
        Path renamedPath = source.resolveSibling(renamedChild);
        Path renamedFilePath = renamedPath.resolve(filename);
        assertTrue(renamedPath.toFile().exists());
        assertFalse(Files.readAllLines(renamedFilePath).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideSourceAndDestPath")
    public void testZipDirectory(Path directory) throws IOException, ExecutionException, InterruptedException {
        String filename = "source.txt";
        Path sourceText = directory.resolve(filename);
        List<String> lines = Arrays.asList("severell", "tester");
        Files.write(sourceText, lines, StandardCharsets.UTF_8);

        InputParameter parameter = mock(InputParameter.class);
        StructureFileOperation operation = new StructureFileOperation(parameter);
        StructureFileOperation spyOperation = spy(operation);
        doReturn(directory).when(spyOperation).getStructureDirectoryPath();

        String zipName = "Zipped Folder";

        Path expectedResult = spyOperation.getZippedDirectory(zipName);
        assertTrue(expectedResult.toFile().exists());
        assertTrue(Files.readAllBytes(expectedResult).length > 0);
    }
}
