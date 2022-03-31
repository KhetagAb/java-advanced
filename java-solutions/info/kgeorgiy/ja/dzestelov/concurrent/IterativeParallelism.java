package info.kgeorgiy.ja.dzestelov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {

    private <T, A, R> R job(int threads,
                            List<? extends T> values,
                            Function<Stream<? extends T>, A> mapper,
                            Function<Stream<A>, R> collector
    ) throws InterruptedException {
        threads = Math.min(threads, Math.max(values.size(), 1));
        int block = values.size() / threads;
        int tail = values.size() % threads;

        List<Thread> workers = new ArrayList<>(threads);
        List<A> midterm = new ArrayList<>(Collections.nCopies(threads, null));
        for (int i = 0, l = 0; i < threads; i++) {
            final int finalI = i;
            final int finalL = l;
            final int finalR = l + block + (tail-- > 0 ? 1 : 0);
            workers.add(new Thread(() -> midterm.set(finalI, mapper.apply(values.subList(finalL, finalR).stream()))));
            workers.get(i).start();
            l = finalR;
        }

        for (Thread worker : workers) {
            worker.join();
        }

        return collector.apply(midterm.stream());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return job(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads,
                              List<? extends T> values,
                              Predicate<? super T> predicate
    ) throws InterruptedException {
        return job(threads, values,
                s -> s.filter(predicate).collect(Collectors.toList()),
                s -> s.flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads,
                              List<? extends T> values,
                              Function<? super T, ? extends U> f
    ) throws InterruptedException {
        return job(threads, values,
                s -> s.map(f).collect(Collectors.toList()),
                s -> s.flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator
    ) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("Values must not be empty");
        }
        return job(threads, values,
                s -> s.max(comparator).orElse(null),
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


    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return job(threads, values,
                s -> s.reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()),
                s -> s.reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()));
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return job(threads, values,
                s -> s.map(lift).reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()),
                s -> s.reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()));
    }
}
