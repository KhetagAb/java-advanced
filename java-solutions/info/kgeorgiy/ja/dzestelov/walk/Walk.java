package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;
import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.SimpleWalker;

import java.nio.charset.StandardCharsets;

public class Walk {

    public static void main(final String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Usage: java Walk <input file> <output file>");
            return;
        }

        try {
            final BaseWalker walker = new SimpleWalker(args[0], args[1], StandardCharsets.UTF_8, "SHA-1");
            walker.walk();
        } catch (final WalkerException e) {
            System.out.println("WalkerException: " + e.getMessage());
        }
    }
}
