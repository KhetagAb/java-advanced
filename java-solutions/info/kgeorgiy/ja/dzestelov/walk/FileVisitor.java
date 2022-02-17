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

    public FileVisitor(FileChecksumBuilder fileChecksum, BufferedWriter writer) {
        this.writer = writer;
        checksumBuilder = fileChecksum;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        writeString(writer, checksumBuilder.getStringChecksum(file) + " " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        writeString(writer, checksumBuilder.getEmptyStringChecksum() + " " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private static void writeString(BufferedWriter writer, String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
