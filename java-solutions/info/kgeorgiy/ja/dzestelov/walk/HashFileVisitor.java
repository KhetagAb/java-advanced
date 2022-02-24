package info.kgeorgiy.ja.dzestelov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class HashFileVisitor extends SimpleFileVisitor<Path> {

    protected final BufferedWriter writer;
    protected final FileChecksumBuilder checksumBuilder;

    public HashFileVisitor(final BufferedWriter writer, final FileChecksumBuilder checksumBuilder) {
        this.writer = writer;
        this.checksumBuilder = checksumBuilder;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        return writeString(checksumBuilder.getStringChecksum(file), file);
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        return writeString(checksumBuilder.getEmptyStringChecksum(), file);
    }

    protected FileVisitResult writeString(final String string, final Path file) throws IOException {
        writeString(string + " " + file);
        return FileVisitResult.CONTINUE;
    }

    protected void writeString(final String string) throws IOException {
        writer.write(string);
        writer.newLine();
    }
}
