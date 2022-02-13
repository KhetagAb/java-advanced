package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksum;
import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;

public class SimpleWalker extends Walker {

    public SimpleWalker(String inputFile, String outputFile, Charset charset, String hashAlgorithmName) throws WalkerException {
        super(inputFile, outputFile, charset, hashAlgorithmName);
    }

    @Override
    protected void process(String line, FileChecksum fileChecksum, BufferedWriter writer) throws IOException {
        writeString(writer, fileChecksum.getFileChecksum(line) + " " + line);
    }
}
