package info.kgeorgiy.ja.dzestelov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Class to apply function to list of arguments in parallel mode.
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final ConcurrentQueue jobs;
    private final List<Thread> workers;

    /**
     * Construct class with number of maximum threads, that can be used during parallel mapping.
     *
     * @param threads maximum count of working threads
     */
    public ParallelMapperImpl(int threads) {
        this.jobs = new ConcurrentQueue(threads);
        this.workers = new ArrayList<>(Collections.nCopies(threads, null));

        for (int i = 0; i < workers.size(); i++) {
            Thread worker = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        jobs.pop().run();
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                Thread.currentThread().interrupt();
            });
            worker.start();
            workers.set(i, worker);
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));

        final int[] counter = new int[]{args.size()};
        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            jobs.push(() -> {
                result.set(finalI, f.apply(args.get(finalI)));
                synchronized (counter) {
                    counter[0]--;
                    counter.notify();
                }
            });
        }

        synchronized (counter) {
            while (counter[0] > 0) {
                counter.wait();
            }
        }

        return result;
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state. Interrupting current thread during closing cause resources leaks.
     */
    @Override
    public void close() {
        workers.forEach(Thread::interrupt);

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }

    static class ConcurrentQueue {

        private final int capacity;
        private final Deque<Runnable> runs;

        ConcurrentQueue(int capacity) {
            this.runs = new ArrayDeque<>();
            this.capacity = capacity;
        }

        private synchronized Runnable pop() throws InterruptedException {
            while (runs.isEmpty()) {
                this.wait();
            }
            Runnable run = runs.remove();
            notifyAll();
            return run;
        }

        private synchronized void push(Runnable run) throws InterruptedException {
            while (runs.size() == capacity) {
                this.wait();
            }
            runs.push(run);
            notifyAll();
        }
    }
}
