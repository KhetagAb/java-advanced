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
            BaseWalker walker;
            if (recursive) {
                walker = new RecursiveWalker(args[0], args[1], CHARSET, HASH_ALGORITHM_NAME);
            } else {
                walker = new SimpleWalker(args[0], args[1], CHARSET, HASH_ALGORITHM_NAME);
            }
            walker.walk();
        } catch (final WalkerException | NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }
}
