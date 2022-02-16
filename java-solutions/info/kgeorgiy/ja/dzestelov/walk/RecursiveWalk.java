package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;
import info.kgeorgiy.ja.dzestelov.walk.walker.RecursiveWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.Walker;

import java.nio.charset.StandardCharsets;

public class RecursiveWalk {

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new WalkerException("Usage: java RecursiveWalk <input file> <output file>");
            }

            Walker walker = new RecursiveWalker(args[0], args[1], StandardCharsets.UTF_8, "SHA-1");
            walker.walk();
        } catch (WalkerException e) {
            System.out.println("WalkerException: " + e.getMessage());
        }
    }
}
