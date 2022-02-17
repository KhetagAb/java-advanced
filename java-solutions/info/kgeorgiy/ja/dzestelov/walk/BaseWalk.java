package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.WalkerException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public abstract class BaseWalk {

    private static final String HASH_ALGORITHM_NAME = "SHA-1";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public void run(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: java " + this.getClass().getName() + " <input file> <output file>");
            return;
        }

        try {
            BaseWalker walker = getWalker(args[0], args[1], CHARSET);
            walker.walk(new FileChecksumBuilder(HASH_ALGORITHM_NAME));
        } catch (NoSuchAlgorithmException | WalkerException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    protected abstract BaseWalker getWalker(String input, String output, Charset charset) throws WalkerException;
}
