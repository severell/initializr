package com.severell.initializr.internal.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

    private static Path buildPath(final Path root, final Path child) {
        if (root == null) {
            return child;
        } else {
            return Paths.get(root.toString(), child.toString());
        }
    }

    private static void addZipDir(final ZipOutputStream out, final Path root, final Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                Path entry = buildPath(root, child.getFileName());
                if (Files.isDirectory(child)) {
                    addZipDir(out, entry, child);
                } else {
                    out.putNextEntry(new ZipEntry(entry.toString()));
                    Files.copy(child, out);
                    out.closeEntry();
                }
            }
        }
    }

    public static void zipDir(final Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory.");
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toString() + ".zip"));

        try (ZipOutputStream out = new ZipOutputStream(bos)) {
            addZipDir(out, path.getFileName(), path);
        }
    }
}
