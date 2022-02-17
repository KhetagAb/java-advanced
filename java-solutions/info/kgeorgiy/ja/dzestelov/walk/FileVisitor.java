package info.kgeorgiy.ja.dzestelov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitor extends SimpleFileVisitor<Path> {

    private final BufferedWriter writer;
    private final FileChecksumBuilder checksumBuilder;

    public FileVisitor(final FileChecksumBuilder fileChecksum, final BufferedWriter writer) {
        this.writer = writer;
        checksumBuilder = fileChecksum;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        writeString(writer, checksumBuilder.getStringChecksum(file) + " " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        writeString(writer, FileChecksumBuilder.getEmptyStringChecksum() + " " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private static void writeString(final BufferedWriter writer, final String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
