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
public class WebCrawler implements AdvancedCrawler {

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
     * Run crawler with given usage.
     * Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]
     *
     * @param args - arguments of crawler
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5) {
            System.out.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }

        int depth;
        int downloads;
        int extractors;
        int perHost;
        try {
            depth = getIArg(args, 1, Integer.MAX_VALUE);
            downloads = getIArg(args, 1, Integer.MAX_VALUE);
            extractors = getIArg(args, 1, Integer.MAX_VALUE);
            perHost = getIArg(args, 1, downloads);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }
        try {
            Downloader downloader = new CachingDownloader(Path.of(""));
            WebCrawler crawler = new WebCrawler(downloader, downloads, extractors, perHost);

            Result download = crawler.download(args[0], depth);
            System.out.println("Downloaded: ");
            download.getDownloaded().forEach(System.out::println);
            System.out.println("Errors: ");
            download.getErrors().forEach((k, v) -> System.out.println(k + ": " + v));

            crawler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getIArg(String[] args, int i, int defaultValue) {
        if (i < args.length) {
            int i1 = Integer.parseInt(args[i]);
            if (i1 > 0) {
                return i1;
            } else {
                throw new IllegalArgumentException("Arguments must be positive");
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Result download(String url, int depth) {
        return downloadVerifyUrls(url, depth, x -> true);
    }

    @Override
    public Result download(String url, int depth, List<String> hosts) {
        Set<String> h = new HashSet<>(hosts);
        return downloadVerifyUrls(url, depth, x -> {
            try {
                return h.contains(getHost(x));
            } catch (MalformedURLException e) {
                return false;
            }
        });
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
        final List<String> downloaded = new ArrayList<>();
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
            List<FutureUrlLinks> futures = new ArrayList<>();
            while (!current.isEmpty()) {
                String currentUrl = current.remove();
                Future<Document> submit = downloadService.submit(() -> getDocument(currentUrl, hosts));
                futures.add(new FutureUrlLinks(currentUrl, extractService.submit(() -> getFutureLinks(submit))));
            }
            for (FutureUrlLinks future : futures) {
                try {
                    for (String u : future.getUrls().get()) {
                        if (!used.contains(u) && urlPredicate.test(u)) {
                            next.add(u);
                            used.add(u);
                        }
                    }
                    downloaded.add(future.getUrl());
                } catch (ExecutionException e) {
                    errors.put(future.getUrl(), wrapException(e));
                } catch (InterruptedException e) {
                    close();
                    break;
                }
            }

            current = next;
        }

        return new Result(downloaded, errors);
    }

    private Document getDocument(String url, ConcurrentMap<String, Semaphore> hosts) throws IOException, InterruptedException {
        String host = getHost(url);
        hosts.putIfAbsent(host, new Semaphore(perHost));
        try {
            hosts.get(host).acquire();
            return downloader.download(url);
        } finally {
            hosts.get(host).release();
        }
    }

    private List<String> getFutureLinks(Future<Document> document) throws IOException, InterruptedException {
        try {
            return document.get().extractLinks();
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

    record FutureUrlLinks(String url, Future<List<String>> urls) {

        private String getUrl() {
            return url;
        }

        private Future<List<String>> getUrls() {
            return urls;
        }
    }
}
