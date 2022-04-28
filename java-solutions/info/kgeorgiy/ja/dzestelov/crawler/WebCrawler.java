package info.kgeorgiy.ja.dzestelov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class that allows to crawl websites.
 */
public class WebCrawler implements Crawler {

    private static final int CLOSE_TIMEOUT_SECONDS = 10;
    private final Downloader downloader;
    private final int perHost;

    private final WrappedCompletionService<UrlDocument> downloadCompletionService;
    private final WrappedCompletionService<List<String>> extractCompletionService;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.perHost = perHost;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
        this.downloadCompletionService = new WrappedCompletionService<>(new ExecutorCompletionService<>(this.downloadService));
        this.extractCompletionService = new WrappedCompletionService<>(new ExecutorCompletionService<>(this.extractService));
    }

    /**
     * toDO
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Downloader downloader = new CachingDownloader(Path.of(""));
            // toDo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHost(final String url) throws MalformedURLException {
        return getURI(url).getHost();
    }

    private static URI getURI(final String url) throws MalformedURLException {
        final String fragmentless = removeFragment(url);
        try {
            final URI uri = new URL(fragmentless).toURI();
            return uri.getPath() == null || uri.getPath().isEmpty() ? new URL(fragmentless + "/").toURI() : uri;
        } catch (final URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    private static String removeFragment(final String url) {
        final int index = url.indexOf('#');
        return index >= 0 ? url.substring(0, index) : url;
    }

    @Override
    public Result download(String url, int depth) {
        return downloadVerifyUrls(url, depth, x -> true);
    }

    private Result downloadVerifyUrls(String url, int depth, Predicate<String> urlPredicate) {
        final List<String> downloaded = new ArrayList<>();
        final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Semaphore> hosts = new ConcurrentHashMap<>();

        final Set<String> used = new HashSet<>();
        Queue<String> current = new ArrayDeque<>();
        Queue<String> next = new ArrayDeque<>();
        if (urlPredicate.test(url)) {
            used.add(url);
            current.add(url);
        }

        Consumer<List<String>> extracted = list -> {
            for (String u : list) {
                if (!used.contains(u) && urlPredicate.test(u)) {
                    next.add(u);
                    used.add(u);
                }
            }
        };

        while (depth-- > 0) {
            while (!current.isEmpty()) {
                String currentUrl = current.remove();
                Future<UrlDocument> add = downloadCompletionService.add(() -> {
                    UrlDocument document = getDocument(currentUrl, errors, hosts);
                    if (document != null) {
                        downloaded.add(document.getUrl());
                    }
                    return document;
                });
                extractCompletionService.add(() -> getFutureLinks(add, errors));
            }
            extractCompletionService.pollAll(extracted);

            current = next;
        }

        return new Result(downloaded, errors);
    }

    private List<String> getFutureLinks(Future<UrlDocument> document, ConcurrentMap<String, IOException> errors) {
        try {
            UrlDocument urlDocument = document.get();
            if (urlDocument == null) {

            }
            return document.extractLinks();
        } catch (Exception e) {
            putError(document.getUrl(), errors, e);
            return null;
        }
    }

    private UrlDocument getDocument(String url, ConcurrentMap<String, IOException> errors, ConcurrentMap<String, Semaphore> hosts) {
        String host = null;
        try {
            host = getHost(url);
            hosts.putIfAbsent(host, new Semaphore(perHost));
            hosts.get(host).acquire();
            UrlDocument document = new UrlDocument(url, downloader.download(url));
            hosts.get(host).release();
            return document;
        } catch (MalformedURLException e) {
            putError(url, errors, e);
            return null;
        } catch (Exception e) {
            hosts.get(host).release();
            putError(url, errors, e);
            return null;
        }
    }

    private void putError(String url, ConcurrentMap<String, IOException> errors, Exception e) {
        errors.put(url, e instanceof IOException ? (IOException) e : new IOException("Cannot download document", e.getCause()));
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        downloadService.shutdown();
        extractService.shutdown();

        try {
            waitUntilAllTasksCompleted(downloadService);
            waitUntilAllTasksCompleted(extractService);
        } catch (InterruptedException e) {
            downloadService.shutdownNow();
            extractService.shutdownNow();
            e.printStackTrace();
        }
    }

    private void waitUntilAllTasksCompleted(ExecutorService service) throws InterruptedException {
        if (!service.awaitTermination(CLOSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            service.shutdownNow();
            System.err.println("[WARNING] WebCrawler terminated before all tasks completed");
        }
    }

    class WrappedCompletionService<T> {

        private final CompletionService<T> completionService;
        private int counter = 0;

        WrappedCompletionService(CompletionService<T> completionService) {
            this.completionService = completionService;
        }

        private Future<T> add(Callable<T> task) {
            counter++;
            return completionService.submit(task);
        }

        private T poll() {
            try {
                T t = completionService.take().get();
                counter--;
                return t;
            } catch (InterruptedException e) {
                close();
                return null;
            } catch (ExecutionException e) {
                throw new IllegalStateException("No error expected" + e.getMessage());
            }
        }

        private void pollAll(Consumer<? super T> consumer) {
            while (!isEmpty()) {
                T e = poll();
                if (e != null) {
                    consumer.accept(e);
                }
            }
        }

        private boolean isEmpty() {
            return counter == 0;
        }
    }

    record UrlDocument(String url, Document document) implements Document {

        private String getUrl() {
            return url;
        }

        @Override
        public List<String> extractLinks() throws IOException {
            return document.extractLinks();
        }
    }
}
