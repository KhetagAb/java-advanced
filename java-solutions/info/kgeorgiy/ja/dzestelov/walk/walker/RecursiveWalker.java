package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.FileVisitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;

public class RecursiveWalker extends BaseWalker {

    public RecursiveWalker(String inputFile, String outputFile, Charset charset) throws WalkerException {
        super(inputFile, outputFile, charset);
    }

    @Override
    protected FileVisitor getFileVisitor(BufferedWriter outputWriter, FileChecksumBuilder fileChecksum) {
        return new FileVisitor(outputWriter, fileChecksum) {
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        };
    }
}
