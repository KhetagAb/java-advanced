package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.RecursiveWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.SimpleWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.WalkerException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class BaseWalk {

    private static final String HASH_ALGORITHM_NAME = "SHA-1";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public void run(String[] args, boolean recursive) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: java " + this.getClass().getName() + " <input file> <output file>");
            return;
        }

        try {
            BaseWalker walker = (recursive ? new RecursiveWalker(args[0], args[1], CHARSET) : new SimpleWalker(args[0], args[1], CHARSET));
            walker.walk(new FileChecksumBuilder(HASH_ALGORITHM_NAME));
        } catch (NoSuchAlgorithmException | WalkerException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }
}
