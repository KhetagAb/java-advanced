package info.kgeorgiy.ja.dzestelov.walk.visitor;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksum;
import info.kgeorgiy.ja.dzestelov.walk.Walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitor extends SimpleFileVisitor<Path> {

    private final BufferedWriter WRITER;
    private final FileChecksum FILE_CHECKSUM;

    public FileVisitor(FileChecksum fileChecksum, BufferedWriter writer) {
        WRITER = writer;
        FILE_CHECKSUM = fileChecksum;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        writeString(WRITER,FILE_CHECKSUM.getFileChecksum(file) + " " + file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        writeString(WRITER,FILE_CHECKSUM.EMPTY_CHECKSUM + " " + file);
        return FileVisitResult.CONTINUE;
    }

    private static void writeString(BufferedWriter writer, String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }
}
