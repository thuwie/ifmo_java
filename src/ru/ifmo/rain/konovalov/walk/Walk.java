package ru.ifmo.rain.konovalov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class Walk {
    private final int CHUNK_SIZE = 1024;
    private final int FNV_32_INIT = 0x811c9dc5;
    private final int FNV_32_PRIME = 0x01000193;


    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Corrupted arguments");
            return;
        }
        String fileIn = args[0];
        String fileOut = args[1];
        File inputFile = new File(fileIn);
        File outputFile = new File(fileOut);
        Set<String> paths;

        Walk walk = new Walk(inputFile, outputFile);

        paths = walk.readFile();
        walk.summarizeChecksums(paths);
    }


    private Walk(File iF, File oF) {
        inputFile = iF;
        outputFile = oF;
    }

    private File inputFile, outputFile;

    private void summarizeChecksums(Set<String> paths) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            for (String path : paths) {
                try (FileInputStream inputStream = new FileInputStream(Paths.get(path).toFile())) {
                    byte[] chunk = new byte[CHUNK_SIZE];
                    int chunkLength;
                    int hv = FNV_32_INIT;
                    while ((chunkLength = inputStream.read(chunk)) != -1) {
                        for (int i = 0; i < chunkLength; i++) {
                            hv = (hv * FNV_32_PRIME) ^ (chunk[i] & 0xff);
                        }
                    }
                    writer.write((String.format("%08x %s\n", hv, path)));
                } catch (IOException | InvalidPathException e) {
                    writer.write(String.format("%08x %s\n", 0, path));
                }
            }
        } catch (IOException e) {
            System.err.println("IO exception L68");
        }
    }

    private Set<String> readFile() {
        Set<String> parsedPaths = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parsedPaths.add(line);
            }
        } catch (InvalidPathException | IOException e) {
            System.err.format("IOException");
        }
        return parsedPaths;
    }
}
