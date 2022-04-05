package info.kgeorgiy.ja.dzestelov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class IterativeParallelism implements AdvancedIP {

    private <T, A, R> R reduceJob(int threads,
                                  List<? extends T> values,
                                  Collector<T, A, R> collector) throws InterruptedException {
        threads = Math.min(threads, Math.max(values.size(), 1));
        int block = values.size() / threads;
        int tail = values.size() % threads;

        List<Thread> workers = new ArrayList<>(Collections.nCopies(threads, null));
        List<A> midterm = new ArrayList<>(Collections.nCopies(threads, null));
        for (int i = 0, l = 0; i < threads; i++) {
            Thread thread = getThread(collector, midterm, values.subList(l, l += block + (tail-- > 0 ? 1 : 0)), i);
            thread.start();
            workers.set(i, thread);
        }

        joinThreads(workers);

        A result = collector.supplier().get();
        for (A subResult : midterm) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                throw new InterruptedException();
            }
            result = collector.combiner().apply(result, subResult);
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

    private void joinThreads(List<Thread> workers) throws InterruptedException {
        InterruptedException exp = null;
        for (int i = 0; i < workers.size(); i++) {
            try {
                workers.get(i).join();
            } catch (InterruptedException e) {
                if (exp == null) {
                    exp = e;
                    // :NOTE: можно только префиксу посылать
                    workers.subList(i, workers.size()).forEach(Thread::interrupt);
                    i--;
                } else {
                    exp.addSuppressed(e);
                }
            }
        }

        if (exp != null) {
            Thread.currentThread().interrupt();
            throw exp;
        }
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return reduceJob(threads, values, Collectors.mapping(Object::toString, Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads,
                              List<? extends T> values,
                              Predicate<? super T> predicate
    ) throws InterruptedException {
        return reduceJob(threads, values, Collectors.filtering(predicate, Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads,
                              List<? extends T> values,
                              Function<? super T, ? extends U> f
    ) throws InterruptedException {
        return reduceJob(threads, values, Collectors.mapping(f, Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads,
                         List<? extends T> values,
                         Comparator<? super T> comparator
    ) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("Values must not be empty");
        }
        return reduceJob(threads, values, Collectors.maxBy(comparator)).orElse(null);
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
        return reduceJob(threads, values, Collectors.reducing(Boolean.FALSE, predicate::test, Boolean::logicalOr));
    }


    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return reduceJob(threads, values, Collectors.mapping(lift, Collectors.reducing(monoid.getIdentity(), monoid.getOperator())));
    }
}
