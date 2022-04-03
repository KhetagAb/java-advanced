package info.kgeorgiy.ja.dzestelov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {

    private <T, A, R> R reduceJob(int threads,
                                  List<? extends T> values,
                                  Collector<T, A, R> collector) throws InterruptedException {
        threads = Math.min(threads, Math.max(values.size(), 1));
        int block = values.size() / threads;
        int tail = values.size() % threads;

        List<Thread> workers = new ArrayList<>(threads);
        List<A> midterm = new ArrayList<>(Collections.nCopies(threads, null));
        for (int i = 0, l = 0; i < threads; i++) {
            Thread thread = getThread(collector, midterm, values.subList(l, l += block + (tail-- > 0 ? 1 : 0)), i);
            thread.start();
            workers.add(thread);
        }

        joinThreads(workers);

        A result = collector.supplier().get();
        for (int i = 1; i < threads; i++) {
            result = collector.combiner().apply(result, midterm.get(i));
        }

        return collector.finisher().apply(result);
    }

    private <T, A, R> Thread getThread(Collector<T, A, R> collector, List<A> midterm, List<? extends T> subList, int index) {
        return new Thread(() -> {
            A container = collector.supplier().get();
            for (T element : subList) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    return;
                }
                collector.accumulator().accept(container, element);
            }
            midterm.set(index, container);
        });
    }

    private void joinThreads(List<Thread> workers) throws InterruptedException { // toDo
        InterruptedException exc = null;
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                if (exc == null) {
                    exc = e;
                } else {
                    exc.addSuppressed(e);
                }
            }
        }

        if (exc != null) {
            throw exc;
        }
    }

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

        joinThreads(workers);

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

        return mapReduce(threads, values, Optional::of, new Monoid<Optional<T>>(null,
                (x, y) -> Collections.max(Arrays.asList(x, y), max))).element();
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
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return job(threads, values,
                s -> s.map(lift).reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()),
                s -> s.reduce(monoid.getIdentity(), monoid.getOperator(), monoid.getOperator()));
    }
}
