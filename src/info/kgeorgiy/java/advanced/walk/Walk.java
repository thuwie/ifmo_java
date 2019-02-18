package info.kgeorgiy.java.advanced.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class Walk {
    public static void main(String[] args) {
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        Set<Path> paths;
        Set<String> checksum;

        Walk walk = new Walk(inputFile, outputFile);

        paths = walk.readFile();
        checksum = walk.calculateChecksums(paths);


        walk.writeFile(checksum);
    }


    private Walk(File iF, File oF) {
        inputFile = iF;
        outputFile = oF;
    }

    private File inputFile;

    private File outputFile;

    private Set<String> calculateChecksums(Set<Path> paths) {
        int CHUNK_SIZE = 1024;
        int FNV_32_INIT = 0x811c9dc5;
        int FNV_32_PRIME = 0x01000193;
        LinkedHashSet<String> checksums = new LinkedHashSet<>();
        for (Path path : paths) {
            try (FileInputStream inputStream = new FileInputStream(path.toFile())) {
                byte[] chunk = new byte[CHUNK_SIZE];
                int chunkLength;
                int hv = FNV_32_INIT;
                while ((chunkLength = inputStream.read(chunk)) != -1) {
                    for (int i = 0; i < chunkLength; i++) {
                        hv = (hv * FNV_32_PRIME) ^ (chunk[i] & 0xff);
                    }
                }
                checksums.add(String.format("%08x %s\n", hv, path));
            } catch (IOException e) {
                checksums.add(String.format("%08x %s\n", 0, path));
            }
        }
        return checksums;
    }

    private Set<Path> readFile() {
        Set<Path> parsedPaths = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parsedPaths.add(Paths.get(line));
            }
        } catch (IOException e) {
            System.err.format("IOException: %s", e);
        }
        return parsedPaths;
    }

    private void writeFile(Set<String> answer) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            for (String path : answer) {
                writer.write(path);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s", e);
        }
    }


}
