package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.WalkerException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class BaseWalker {

    private final FileChecksumBuilder fileChecksum;

    private final Charset charset;
    private final Path input;
    private final Path output;

    public BaseWalker(final String inputFile, final String outputFile, final Charset charset, final FileChecksumBuilder checksumBuilder) throws WalkerException {
        if (inputFile == null || outputFile == null) {
            throw new WalkerException("Input and output file names must be not null");
        }

        this.input = getPath(inputFile, "input");
        this.output = getPath(outputFile, "output");

        if (charset == null || checksumBuilder == null) {
            throw new WalkerException("Charset and FileChecksumBuilder must be not null");
        }

        this.fileChecksum = checksumBuilder;
        this.charset = charset;
    }

    private static Path getPath(final String fileNAme, final String name) throws WalkerException {
        try {
            return Path.of(fileNAme);
        } catch (final InvalidPathException e) {
            throw new WalkerException("Invalid " + name + " file name: " + e.getMessage(), e);
        }
    }

    public void walk() throws WalkerException {
        final Path parent = this.output.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ignored) {
            }
        }

        try (final BufferedReader inputReader = Files.newBufferedReader(input, charset)) {
            try (final BufferedWriter outputWriter = Files.newBufferedWriter(output, charset)) {
                String line;
                while ((line = readInputFileLine(inputReader)) != null) {
                    try {
                        process(Path.of(line), fileChecksum, outputWriter);
                    } catch (InvalidPathException e) {
                        writeString(outputWriter, fileChecksum.getEmptyStringChecksum() + " " + line);
                    }
                }
            } catch (final IOException | SecurityException e) {
                throw new WalkerException("Unable to write data to output file: " + e.getMessage(), e);
            }
        } catch (final IOException | SecurityException e) {
            throw new WalkerException("Unable to read input file: " + e.getMessage(), e);
        }
    }

    private String readInputFileLine(final BufferedReader bufferedReader) throws WalkerException {
        try {
            return bufferedReader.readLine();
        } catch (final IOException e) {
            throw new WalkerException("Unable to read line from input file: " + e.getMessage(), e);
        }
    }

    protected abstract void process(Path line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException;

    protected static void writeString(final BufferedWriter writer, final String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}