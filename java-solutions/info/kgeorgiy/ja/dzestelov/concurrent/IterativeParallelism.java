package info.kgeorgiy.ja.dzestelov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    private <T, A, R> R job(int threads,
                            List<? extends T> values,
                            Function<Stream<? extends T>, A> toWorkers,
                            Function<Stream<A>, R> fromWorkers
    ) throws InterruptedException {
        threads = Math.min(threads, Math.max(values.size(), 1));
        int block = values.size() / threads;
        int tail = values.size() % threads;

        List<Thread> workers = new ArrayList<>(threads);
        List<A> midterm = new ArrayList<>(Collections.nCopies(threads, null));
        for (int i = 0, l = 0; i < threads; i++) {
            int finalI = i;
            int finalL = l;
            int finalR = l + block + (tail-- > 0 ? 1 : 0);
            workers.add(new Thread(() -> midterm.set(finalI, toWorkers.apply(values.subList(finalL, finalR).stream()))));
            workers.get(i).start();
            l = finalR;
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                worker.interrupt();
                throw e;
            }
        }

        return fromWorkers.apply(midterm.stream());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
//        return reduceJob(threads, values, s -> s.map(Objects::toString), Collectors.joining());
        return job(threads, values,
                s -> s.map(Objects::toString),
                s -> s.reduce(Stream::concat)
                        .orElse(Stream.empty())
                        .collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads,
                              List<? extends T> values,
                              Predicate<? super T> predicate
    ) throws InterruptedException {
        return job(threads, values,
                s -> s.filter(predicate),
                s -> s.reduce(Stream::concat)
                        .orElseGet(Stream::empty)
                        .collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads,
                              List<? extends T> values,
                              Function<? super T, ? extends U> f
    ) throws InterruptedException {
        return job(threads, values,
                s -> s.map(f),
                s -> s.reduce(Stream::concat)
                        .orElse(Stream.empty())
                        .collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator
    ) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("Values must not be empty");
        }
        return job(threads, values, s -> s.max(comparator).orElse(null),
                s -> s.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator
    ) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads,
                           List<? extends T> values,
                           Predicate<? super T> predicate
    ) throws InterruptedException {
        return !any(threads, values, Predicate.not(predicate));
    }

    @Override
    public <T> boolean any(int threads,
                           List<? extends T> values,
                           Predicate<? super T> predicate
    ) throws InterruptedException {
        return job(threads, values,
                s -> s.anyMatch(predicate),
                s -> s.anyMatch(Predicate.isEqual(true)));
    }
}
