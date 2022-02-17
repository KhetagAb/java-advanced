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

    private final MessageDigest messageDigest;

    public FileChecksumBuilder(final String hashAlgorithmName) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(hashAlgorithmName);
    }

    public String getStringChecksum(String file) {
        return toString(getChecksum(file));
    }

    public String getStringChecksum(final Path path) {
        return toString(getChecksum(path));
    }

    public static String getEmptyStringChecksum() {
        return toString(getEmptyChecksum());
    }

    public byte[] getChecksum(String file) {
        try {
            return getChecksum(Path.of(file));
        } catch (final InvalidPathException e) {
            return getEmptyChecksum();
        }
    }

    public byte[] getChecksum(final Path path) {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            int read;
            final byte[] buff = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buff)) != -1) {
                messageDigest.update(buff, 0, read);
            }
            return messageDigest.digest();
        } catch (IOException | SecurityException e) {
            messageDigest.reset();
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
