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
import java.util.function.Predicate;

/**
 * Class that allows to crawl websites.
 */
public class WebCrawler implements Crawler {

    private static final int CLOSE_TIMEOUT_SECONDS = 10;
    private final Downloader downloader;
    private final int perHost;

    private final ExecutorService downloadService;
    private final ExecutorService extractService;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.perHost = perHost;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
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

    @Override
    public Result download(String url, int depth) {
        return downloadVerifyUrls(url, depth, x -> true);
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

    private Result downloadVerifyUrls(String url, int depth, Predicate<String> urlPredicate) {
        final Queue<String> downloaded = new ConcurrentLinkedQueue<>();
        final ConcurrentMap<String, Semaphore> hosts = new ConcurrentHashMap<>();

        final Map<String, IOException> errors = new HashMap<>();

        final Set<String> used = new HashSet<>();
        Queue<String> current = new ArrayDeque<>();
        Queue<String> next = new ArrayDeque<>();
        if (urlPredicate.test(url)) {
            used.add(url);
            current.add(url);
        }

        while (depth-- > 0) {
            List<UrlLinks> futures = new ArrayList<>();
            while (!current.isEmpty()) {
                String currentUrl = current.remove();
                futures.add(new UrlLinks(currentUrl,
                        extractService.submit(() -> getFutureLinks(
                                downloadService.submit(() -> {
                                            UrlDocument document = getDocument(currentUrl, hosts);
                                            downloaded.add(document.getUrl());
                                            return document;
                                        }
                                ))
                        ))
                );
            }
            for (UrlLinks future : futures) {
                try {
                    for (String u : future.getUrls().get()) {
                        if (!used.contains(u) && urlPredicate.test(u)) {
                            next.add(u);
                            used.add(u);
                        }
                    }
                } catch (ExecutionException e) {
                    errors.put(future.getUrl(), wrapException(e));
                } catch (InterruptedException e) {
                    close();
                    break;
                }
            }

            current = next;
        }

        return new Result(downloaded.stream().toList(), errors);
    }

    private UrlDocument getDocument(String url, ConcurrentMap<String, Semaphore> hosts) throws IOException, InterruptedException {
        String host = getHost(url);
        hosts.putIfAbsent(host, new Semaphore(perHost));
        try {
            hosts.get(host).acquire();
            return new UrlDocument(url, downloader.download(url));
        } finally {
            hosts.get(host).release();
        }
    }

    private List<String> getFutureLinks(Future<UrlDocument> document) throws IOException, InterruptedException {
        try {
            return document.get().document.extractLinks();
        } catch (ExecutionException e) {
            throw wrapException(e);
        }
    }

    private IOException wrapException(ExecutionException e) {
        return e.getCause() instanceof IOException ? (IOException) e.getCause() : new IOException(e.getCause());
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

    record UrlDocument(String url, Document document) implements Document {

        private String getUrl() {
            return url;
        }

        @Override
        public List<String> extractLinks() throws IOException {
            return document.extractLinks();
        }
    }

    record UrlLinks(String url, Future<List<String>> urls) {

        private String getUrl() {
            return url;
        }

        private Future<List<String>> getUrls() {
            return urls;
        }
    }
}
