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

/**
 * Class that allows to crawl websites.
 */
public class WebCrawler implements Crawler {

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
        this.downloadCompletionService = new WrappedCompletionService<>(this.downloadService);
        this.extractCompletionService = new WrappedCompletionService<>(this.extractService);
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

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        List<String> downloaded = new ArrayList<>();
        ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();

        Set<String> used = new HashSet<>();
        Queue<List<String>> current = new ArrayDeque<>();
        current.add(List.of(url));

        Consumer<UrlDocument> downed = document -> {
            downloaded.add(document.getUrl());
            extractCompletionService.add(() -> getLinks(document, errors));
        };

        while (depth-- > 0) {
            Queue<List<String>> next = new ArrayDeque<>();
            while (!current.isEmpty()) {
                for (String currentUrl : current.poll()) {
                    if (!used.contains(currentUrl)) {
                        used.add(currentUrl);
                        downloadCompletionService.add(() -> getDocument(currentUrl, errors));
                    }
                }

                downloadCompletionService.pollAll(downed);
                extractCompletionService.pollAll(next::add);
            }

            current = next;
        }

        return new Result(downloaded, errors);
    }

    private List<String> getLinks(UrlDocument document, ConcurrentMap<String, IOException> errors) {
        try {
            return document.extractLinks();
        } catch (IOException e) {
            errors.put(document.getUrl(), e);
            return null;
        }
    }

    private UrlDocument getDocument(String url, ConcurrentMap<String, IOException> errors) {
        try {
            return new UrlDocument(url, downloader.download(url));
        } catch (IOException e) {
            errors.put(url, e);
            return null;
        }
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {

    }

    static class WrappedCompletionService<T> {

        private final ExecutorService executorService;
        private final Queue<Future<T>> queue;
        private int counter = 0;

        WrappedCompletionService(ExecutorService executorService) {
            this.executorService = executorService;
            this.queue = new ArrayDeque<>();
        }

        private void add(Callable<T> task) {
            counter++;
            queue.add(executorService.submit(task));
        }

        private T poll() {
            try {
                T t = queue.remove().get();
                counter--;
                return t;
            } catch (ExecutionException e) {
                System.out.println("exec");
                return null;
            } catch (InterruptedException e) {
                System.out.println("interrupted");
                return null;
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
