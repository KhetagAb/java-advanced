package info.kgeorgiy.ja.dzestelov.walk.walker;

import info.kgeorgiy.ja.dzestelov.walk.FileChecksumBuilder;
import info.kgeorgiy.ja.dzestelov.walk.WalkerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class SimpleWalker extends BaseWalker {

    public SimpleWalker(String inputFile, String outputFile, Charset charset, String hashAlgorithmName) throws WalkerException, NoSuchAlgorithmException {
        super(inputFile, outputFile, charset, hashAlgorithmName);
    }

    @Override
    protected void process(String line, FileChecksumBuilder fileChecksum, BufferedWriter writer) throws IOException {
        writeString(writer, fileChecksum.getStringChecksum(line) + " " + line);
    }
}
