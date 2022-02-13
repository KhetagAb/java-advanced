package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksum;
import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import java.security.NoSuchAlgorithmException;

public abstract class Walker {

    private final FileChecksum fileChecksum;

    private final Charset CHARSET;
    private final Path input;
    private final Path output;

    public Walker(String inputFile, String outputFile, Charset charset, String hashAlgorithmName) throws WalkerException {
        if (inputFile == null || outputFile == null) {
            throw new WalkerException("Input and output files must be not null");
        }

        if (hashAlgorithmName == null) {
            throw new WalkerException("Hash algorithm required");
        }

        try {
            this.output = Path.of(outputFile);
            this.input = Path.of(inputFile);
        } catch (InvalidPathException e) {
            throw new WalkerException("Invalid input and/or output files");
        }

        Path parent = this.output.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new WalkerException("Unable to create output file parent directories");
            }
        }

        try {
            this.fileChecksum = new FileChecksum(hashAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new WalkerException("Unable to load " + hashAlgorithmName + " algorithm");
        }
        this.CHARSET = charset;
    }

    public void walk() throws WalkerException {
        try (BufferedReader inputReader = Files.newBufferedReader(input, CHARSET)) {
            try (BufferedWriter outputWriter = Files.newBufferedWriter(output, CHARSET)) {
                String line;
                while ((line = inputReader.readLine()) != null) {
                    process(line, fileChecksum, outputWriter);
                }
            } catch (IOException | SecurityException e) {
                throw new WalkerException("Unable to write data into output file");
            }
        } catch (IOException | SecurityException e) {
            throw new WalkerException("Unable to read input file");
        }
    }

    protected abstract void process(String line, FileChecksum fileChecksum, BufferedWriter writer) throws IOException;

    protected static void writeString(BufferedWriter writer, String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
