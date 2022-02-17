package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.FileVisitor;
import info.kgeorgiy.ja.dzestelov.walk.WalkerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecursiveWalker extends BaseWalker {

    public RecursiveWalker(String inputFile, String outputFile, Charset charset, FileChecksumBuilder checksumBuilder) throws WalkerException {
        super(inputFile, outputFile, charset, checksumBuilder);
    }

    @Override
    protected void process(Path line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException {
        Files.walkFileTree(line, new FileVisitor(fileChecksum, writer));
    }
}
