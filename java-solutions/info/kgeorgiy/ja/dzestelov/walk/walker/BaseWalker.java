package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.FileVisitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class BaseWalker {

    private final Charset charset;
    private final Path input;
    private final Path output;

    public BaseWalker(final String inputFile, final String outputFile, final Charset charset) throws WalkerException {
        if (inputFile == null || outputFile == null) {
            throw new NullPointerException("Input and output file names must be not null");
        }

        if (charset == null) {
            throw new NullPointerException("Charset must be not null");
        }

        this.input = getPath(inputFile, "input");
        this.output = getPath(outputFile, "output");
        this.charset = charset;
    }

    private static Path getPath(final String fileNAme, final String name) throws WalkerException {
        try {
            return Path.of(fileNAme);
        } catch (final InvalidPathException e) {
            throw new WalkerException("Invalid " + name + " file name: " + e.getMessage(), e);
        }
    }

    public void walk(final FileChecksumBuilder fileChecksum) throws WalkerException {
        if (fileChecksum == null) {
            throw new NullPointerException("File visitor and checksum builder must be not null");
        }

        final Path parent = this.output.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ignored) {
                // empty
            }
        }

        try (final BufferedReader inputReader = Files.newBufferedReader(input, charset)) {
            try (final BufferedWriter outputWriter = Files.newBufferedWriter(output, charset)) {
                FileVisitor fileVisitor = getFileVisitor(outputWriter, fileChecksum);

                String line;
                while ((line = readInputFileLine(inputReader)) != null) {
                    try {
                        Files.walkFileTree(Path.of(line), fileVisitor);
                    } catch (InvalidPathException e) {
                        writeString(outputWriter, fileChecksum.getEmptyStringChecksum() + " " + line);
                    }
                }
            } catch (final IOException e) {
                throw new WalkerException("Unable to write data to output file: " + e.getMessage(), e);
            }
        } catch (final IOException e) {
            throw new WalkerException("Unable to read input file: " + e.getMessage(), e);
        }
    }

    protected abstract FileVisitor getFileVisitor(BufferedWriter outputWriter, FileChecksumBuilder fileChecksum);

    private String readInputFileLine(final BufferedReader bufferedReader) throws WalkerException {
        try {
            return bufferedReader.readLine();
        } catch (final IOException e) {
            throw new WalkerException("Unable to read line from input file: " + e.getMessage(), e);
        }
    }

    protected static void writeString(final BufferedWriter writer, final String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
