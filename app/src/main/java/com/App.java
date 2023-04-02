package com;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class App {

    public static void processJarFilesInDirectory(String directory) {
        Path startPath = Paths.get(directory);
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().contains("ClassParser.jar")) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (file.toString().endsWith(".jar")) {
                        createJsonFileForJar(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error reading directory: " + directory);
            e.printStackTrace();
        }
    }

    private static void createJsonFileForJar(Path jarPath) {
        Path jsonPath = jarPath.resolveSibling(jarPath.getFileName().toString().replace(".jar", ".json"));
        File jar = jarPath.toFile();
        String jsonFlattened = new JarFileParser(jar).getClassesJsonArray();
        String jsonNested = new RecursiveJsonUnflattener(jsonFlattened).getNested();
        try {
            Files.write(jsonPath, jsonNested.getBytes());
            System.out.println("Successfully written to json file: " + jsonPath.toString());
        } catch (IOException e) {
            System.err.println("Error creating JSON file: " + jsonPath.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        processJarFilesInDirectory(".");
    }

}
