package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;
import info.kgeorgiy.ja.dzestelov.walk.walker.SimpleWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.Walker;

import java.nio.charset.StandardCharsets;

public class Walk {
    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) {
                throw new WalkerException("Usage: java Walk <input file> <output file>");
            }

            Walker walker = new SimpleWalker(args[0], args[1], StandardCharsets.UTF_8, "SHA-1");
            walker.walk();
        } catch (WalkerException e) {
            System.out.println("WalkerException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
