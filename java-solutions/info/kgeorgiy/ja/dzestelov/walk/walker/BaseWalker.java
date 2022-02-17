package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import java.security.NoSuchAlgorithmException;

public abstract class BaseWalker {

    private final FileChecksumBuilder fileChecksum;

    private final Charset charset;
    private final Path input;
    private final Path output;

    public BaseWalker(final String inputFile, final String outputFile, final Charset charset, final String hashAlgorithmName) throws WalkerException {
        if (inputFile == null || outputFile == null) {
            throw new WalkerException("Input and output file names must be not null");
        }

        if (hashAlgorithmName == null) {
            throw new WalkerException("Hash algorithm required");
        } else {
            try {
                this.fileChecksum = new FileChecksumBuilder(hashAlgorithmName);
            } catch (final NoSuchAlgorithmException e) {
                throw new WalkerException("Unable to load " + hashAlgorithmName + " algorithm");
            }
        }

        try {
            this.output = Path.of(outputFile);
            this.input = Path.of(inputFile);
        } catch (final InvalidPathException e) {
            throw new WalkerException("Invalid input or output files");
        }

        final Path parent = this.output.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                throw new WalkerException("Unable to create output file parent directories");
            }
        }

        this.charset = charset;
    }

    public void walk() throws WalkerException {
        try (final BufferedReader inputReader = Files.newBufferedReader(input, charset);
             final BufferedWriter outputWriter = Files.newBufferedWriter(output, charset)) {
            String line;
            while ((line = inputReader.readLine()) != null) {
                process(line, fileChecksum, outputWriter);
            }
        } catch (final IOException | SecurityException e) {
            throw new WalkerException("Unable to read input file or write data to output file");
        }
    }

    protected abstract void process(String line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException;

    protected static void writeString(final BufferedWriter writer, final String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
