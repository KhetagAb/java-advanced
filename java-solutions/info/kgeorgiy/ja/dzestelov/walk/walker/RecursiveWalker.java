package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.FileVisitor;
import info.kgeorgiy.ja.dzestelov.walk.WalkerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class RecursiveWalker extends BaseWalker {

    public RecursiveWalker(final String inputFile, final String outputFile, final Charset charset, final String hashAlgorithmName) throws WalkerException, NoSuchAlgorithmException {
        super(inputFile, outputFile, charset, hashAlgorithmName);
    }

    @Override
    protected void process(final String line, final FileChecksumBuilder fileChecksum, final BufferedWriter writer) throws IOException {
        try {
            Files.walkFileTree(Path.of(line), new FileVisitor(fileChecksum, writer));
        } catch (final InvalidPathException e) {
            writeString(writer, fileChecksum.getEmptyStringChecksum() + " " + line);
        }
    }
}
