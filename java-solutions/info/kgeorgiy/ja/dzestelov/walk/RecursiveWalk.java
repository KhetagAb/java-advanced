package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.exception.WalkerException;
import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.RecursiveWalker;

import java.nio.charset.StandardCharsets;

// :NOTE: Копипаста
public class RecursiveWalk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null)  {
            System.out.println("Usage: java RecursiveWalk <input file> <output file>");
            return;
        }

        try {
            BaseWalker walker = new RecursiveWalker(args[0], args[1], StandardCharsets.UTF_8, "SHA-1");
            walker.walk();
        } catch (WalkerException e) {
            System.out.println("WalkerException: " + e.getMessage());
        }
    }
}
