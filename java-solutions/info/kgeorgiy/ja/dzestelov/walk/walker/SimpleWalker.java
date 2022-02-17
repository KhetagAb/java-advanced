package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.WalkerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class SimpleWalker extends BaseWalker {

    public SimpleWalker(String inputFile, String outputFile, Charset charset, FileChecksumBuilder checksumBuilder) throws WalkerException {
        super(inputFile, outputFile, charset, checksumBuilder);
    }

    @Override
    protected void process(Path line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException {
        writeString(writer, fileChecksum.getStringChecksum(line) + " " + line);
    }
}
