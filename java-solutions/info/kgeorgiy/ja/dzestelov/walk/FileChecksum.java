package info.kgeorgiy.ja.dzestelov.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileChecksum {

    public final String EMPTY_CHECKSUM = "0000000000000000000000000000000000000000";
    private final int BUFFER_SIZE = 1 << 16;

    private final MessageDigest MESSAGE_DIGEST;

    public FileChecksum(String hashAlgorithmName) throws NoSuchAlgorithmException {
        MESSAGE_DIGEST = MessageDigest.getInstance(hashAlgorithmName);
    }

    public String getFileChecksum(String file) {
        try {
            return getFileChecksum(Path.of(file));
        } catch (InvalidPathException e) {
            return EMPTY_CHECKSUM;
        }
    }

    public String getFileChecksum(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            int read;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buff)) != -1) {
                MESSAGE_DIGEST.update(buff, 0, read);
            }
            return HexFormat.of().formatHex(MESSAGE_DIGEST.digest());
        } catch (IOException e) {
            MESSAGE_DIGEST.reset();
            return EMPTY_CHECKSUM;
        }
    }
}
