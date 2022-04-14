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

        Runnable workerRun = () -> {
            while (!Thread.interrupted()) {
                try {
                    jobs.pop().run();
                } catch (InterruptedException e) {
                    break;
                }
            }

            Thread.currentThread().interrupt();
        };

        for (int i = 0; i < workers.size(); i++) {
            Thread worker = new Thread(workerRun);
            worker.start();
            workers.set(i, worker);
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));

        List<Job> currentJobs = new ArrayList<>(Collections.nCopies(args.size(), null));
        ConcurrentCounter counter = new ConcurrentCounter(args.size());
        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            Job job = new Job(() -> result.set(finalI, f.apply(args.get(finalI))), counter);
            currentJobs.set(i, job);
            jobs.push(job);
        }

        counter.waitUntil();
        checkJobsForExceptions(currentJobs);

        return result;
    }

    private void checkJobsForExceptions(List<Job> currentJobs) throws InterruptedException {
        Exception exp = null;
        for (Job job : currentJobs) {
            Exception e = job.getException();
            if (e != null) {
                if (exp == null) {
                    exp = e;
                } else {
                    exp.addSuppressed(e);
                }
            }
        }
        if (exp != null) {
            InterruptedException toThrow = new InterruptedException("Exception handled during jobs execution");
            toThrow.addSuppressed(exp);
            throw toThrow;
        }
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

    class Job {

        private final Runnable run;
        private final ConcurrentCounter counter;

        private Exception exception = null;

        private Job(Runnable run, ConcurrentCounter counter) {
            this.run = run;
            this.counter = counter;
        }

        private synchronized void run() {
            try {
                run.run();
            } catch (Exception e) {
                exception = e;
            } finally {
                counter.decrement();
            }
        }

        private synchronized Exception getException() {
            return this.exception;
        }
    }

    class ConcurrentCounter {

        private int counter;

        private ConcurrentCounter(int counter) {
            this.counter = counter;
        }

        private synchronized void decrement() {
            --counter;
            this.notify();
        }

        private synchronized void waitUntil() throws InterruptedException {
            while (counter != 0) {
                this.wait();
            }
        }
    }

    static class ConcurrentQueue {

        private final int capacity;
        private final Deque<Job> runs;

        ConcurrentQueue(int capacity) {
            this.runs = new ArrayDeque<>();
            this.capacity = capacity;
        }

        private synchronized Job pop() throws InterruptedException {
            while (runs.isEmpty()) {
                this.wait();
            }
            Job run = runs.remove();
            notifyAll();
            return run;
        }

        private synchronized void push(Job run) throws InterruptedException {
            while (runs.size() == capacity) {
                this.wait();
            }
            runs.push(run);
            notifyAll();
        }
    }
}
