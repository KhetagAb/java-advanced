package info.kgeorgiy.ja.dzestelov.walk;

import info.kgeorgiy.ja.dzestelov.walk.walker.BaseWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.SimpleWalker;
import info.kgeorgiy.ja.dzestelov.walk.walker.WalkerException;

import java.nio.charset.Charset;

public class Walk extends BaseWalk {

    public static void main(final String[] args) {
        new Walk().run(args);
    }

    @Override
    protected BaseWalker getWalker(String input, String output, Charset charset) throws WalkerException {
        return new SimpleWalker(input, output, charset);
    }
}
