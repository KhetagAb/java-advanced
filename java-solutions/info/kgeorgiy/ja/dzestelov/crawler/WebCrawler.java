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
import java.util.stream.Collectors;

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
            downloads = getIArg(args, 2, Integer.MAX_VALUE);
            extractors = getIArg(args, 3, Integer.MAX_VALUE);
            perHost = getIArg(args, 4, downloads);
        } catch (IllegalArgumentException e) {
            System.out.println("Wrong arguments: " + e.getMessage());
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
            System.out.println("Cannot create path for CachingDownloader");
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
        Set<String> h = hosts.stream().collect(Collectors.toUnmodifiableSet());
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
        final ConcurrentMap<String, HostQueue> hostQueue = new ConcurrentHashMap<>();

        final Map<String, IOException> errors = new HashMap<>();

        final Set<String> used = new HashSet<>();
        Queue<String> current = new ArrayDeque<>();
        if (urlPredicate.test(url)) {
            used.add(url);
            current.add(url);
        }

        Queue<FutureUrlLinks> futures = new LinkedBlockingQueue<>();
        while (depth-- > 0) {
            while (!current.isEmpty()) {
                String currentUrl = current.remove();
                try {
                    Url u = new Url(currentUrl, getHost(url));
                    submitUrl(u, hostQueue, futures);
                } catch (MalformedURLException e) {
                    errors.put(currentUrl, wrapException(e));
                }
            }
            Queue<String> next = new ArrayDeque<>();
            while (!futures.isEmpty()) {
                FutureUrlLinks future = futures.poll();
                try {
                    for (String u : future.getUrls().get()) {
                        if (!used.contains(u) && urlPredicate.test(u)) {
                            next.add(u);
                            used.add(u);
                        }
                    }
                    downloaded.add(future.getUrl().toString());
                } catch (ExecutionException e) {
                    errors.put(future.getUrl().toString(), wrapException(e));
                } catch (InterruptedException e) {
                    close();
                    break;
                }
            }

            current = next;
        }

        return new Result(downloaded, errors);
    }

    private void submitUrl(Url url, ConcurrentMap<String, HostQueue> hosts, Queue<FutureUrlLinks> futures) {
        String host = url.getHost();
        hosts.putIfAbsent(host, new HostQueue());
        hosts.compute(host, (h, q) -> {
            if (q.busy == perHost) {
                q.queue.add(url);
            } else {
                Callable<List<String>> action = () -> {
                    return getFutureLinks(downloadService.submit(() -> {
                                hosts.compute(host, (ch, cq) -> {
                                    cq.busy++;
                                    return cq;
                                });
                                Document download = downloader.download(url.toString());
                                hosts.compute(host, (ch, cq) -> {
                                    cq.busy--;
                                    if (!cq.queue.isEmpty()) {
                                        submitUrl(cq.queue.poll(), hosts, futures);
                                    }
                                    return cq;
                                });
                                return download;
                            }
                    ));
                };
                futures.add(new FutureUrlLinks(url, extractService.submit(action)));
            }
            return q;
        });
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

    private IOException wrapException(Exception e) {
        return e instanceof IOException ? (IOException) e : new IOException(e);
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

    private static class HostQueue {

        private final Queue<Url> queue = new ArrayDeque<>();
        private int busy = 0;
    }

    record Url(String url, String host) {

        @Override
        public String toString() {
            return url;
        }

        private String getHost() {
            return url;
        }
    }

    record FutureUrlLinks(Url url, Future<List<String>> urls) {

        private Url getUrl() {
            return url;
        }

        private Future<List<String>> getUrls() {
            return urls;
        }
    }
}
