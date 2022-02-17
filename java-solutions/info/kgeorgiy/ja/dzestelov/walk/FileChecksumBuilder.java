package info.kgeorgiy.ja.dzestelov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileChecksumBuilder {

    private static final int EMPTY_CHECKSUM_SIZE = 20;
    private static final int BUFFER_SIZE = 1 << 16;

    private final MessageDigest messageDigest;

    public FileChecksumBuilder(String hashAlgorithmName) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(hashAlgorithmName);
    }

    public String getStringChecksum(Path path) {
        return toString(getChecksum(path));
    }

    public String getEmptyStringChecksum() {
        return toString(getEmptyChecksum());
    }

    public byte[] getChecksum(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            int read;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buff)) != -1) {
                messageDigest.update(buff, 0, read);
            }
            return messageDigest.digest();
        } catch (IOException | SecurityException e) {
            messageDigest.reset();
            return getEmptyChecksum();
        }
    }

    public byte[] getEmptyChecksum() {
        return new byte[EMPTY_CHECKSUM_SIZE];
    }

    private String toString(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
