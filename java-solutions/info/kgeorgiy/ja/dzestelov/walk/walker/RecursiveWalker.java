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

    public RecursiveWalker(String inputFile, String outputFile, Charset charset, String hashAlgorithmName) throws WalkerException, NoSuchAlgorithmException {
        super(inputFile, outputFile, charset, hashAlgorithmName);
    }

    @Override
    protected void process(String line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException {
        try {
            FileVisitor fileVisitor = new FileVisitor(fileChecksum, writer);
            Files.walkFileTree(Path.of(line), fileVisitor);
        } catch (InvalidPathException e) {
            writeString(writer, FileChecksumBuilder.getEmptyStringChecksum() + " " + line);
        }
    }
}
