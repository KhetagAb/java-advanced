package info.kgeorgiy.ja.dzestelov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileChecksumBuilder {

    private static final int EMPTY_CHECKSUM_SIZE = 20;
    private static final int BUFFER_SIZE = 1 << 16;

    private final MessageDigest MESSAGE_DIGEST;

    public FileChecksumBuilder(String hashAlgorithmName) throws NoSuchAlgorithmException {
        MESSAGE_DIGEST = MessageDigest.getInstance(hashAlgorithmName);
    }

    public String getStringChecksum(String file) {
        return toString(getChecksum(file));
    }

    public String getStringChecksum(Path path) {
        return toString(getChecksum(path));
    }

    public static String getEmptyStringChecksum() {
        return toString(getEmptyChecksum());
    }

    public byte[] getChecksum(String file) {
        try {
            return getChecksum(Path.of(file));
        } catch (InvalidPathException e) {
            return getEmptyChecksum();
        }
    }

    public byte[] getChecksum(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            int read;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buff)) != -1) {
                MESSAGE_DIGEST.update(buff, 0, read);
            }
            return MESSAGE_DIGEST.digest();
        } catch (IOException | SecurityException e) {
            MESSAGE_DIGEST.reset();
            return getEmptyChecksum();
        }
    }

    public static byte[] getEmptyChecksum() {
        return new byte[EMPTY_CHECKSUM_SIZE];
    }

    private static String toString(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
