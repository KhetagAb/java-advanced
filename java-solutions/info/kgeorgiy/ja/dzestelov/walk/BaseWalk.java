package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.RecursiveWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.SimpleWalker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class BaseWalk {
    private static final String HASH_ALGORITHM_NAME = "SHA-1";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    protected static void runWalk(String[] args, boolean recursive) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: java " + (recursive ? "RecursiveWalk" : "Walk") + " <input file> <output file>");
            return;
        }

        try {
            FileChecksumBuilder fileChecksumBuilder = new FileChecksumBuilder(HASH_ALGORITHM_NAME);
            BaseWalker walker;
            if (recursive) {
                walker = new RecursiveWalker(args[0], args[1], CHARSET, fileChecksumBuilder);
            } else {
                walker = new SimpleWalker(args[0], args[1], CHARSET, fileChecksumBuilder);
            }
            walker.walk();
        } catch (NoSuchAlgorithmException | WalkerException e) {
            System.out.println(e.getMessage());
        }
    }
}
