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
    private static final byte[] buffer = new byte[BUFFER_SIZE];

    private final MessageDigest messageDigest;

    public FileChecksumBuilder(final String hashAlgorithmName) throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(hashAlgorithmName);
    }

    public String getStringChecksum(final Path path) {
        return toString(getChecksum(path));
    }

    public byte[] getChecksum(final Path path) {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            messageDigest.reset();
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, read);
            }
            return messageDigest.digest();
        } catch (final IOException e) {
            return getEmptyChecksum();
        }
    }

    public String getEmptyStringChecksum() {
        return toString(getEmptyChecksum());
    }

    public static byte[] getEmptyChecksum() {
        return new byte[EMPTY_CHECKSUM_SIZE];
    }

    private String toString(final byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
