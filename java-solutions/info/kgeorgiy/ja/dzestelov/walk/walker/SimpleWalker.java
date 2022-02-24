package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.HashFileVisitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleWalker extends BaseWalker {

    public SimpleWalker(final String inputFile, final String outputFile, final Charset charset) throws WalkerException {
        super(inputFile, outputFile, charset);
    }

    @Override
    protected HashFileVisitor getFileVisitor(final BufferedWriter outputWriter, final FileChecksumBuilder fileChecksum) {
        return new HashFileVisitor(outputWriter, fileChecksum) {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                writeString(fileChecksum.getEmptyStringChecksum() + " " + dir.toString());
                return FileVisitResult.SKIP_SUBTREE;
            }
        };
    }
}
