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

/**
 * Class that allows to crawl websites.
 */
public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final int perHost;

    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final CompletionService<Document> downloadCompletionService;
    private final CompletionService<List<String>> extractCompletionService;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.perHost = perHost;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.extractService = Executors.newFixedThreadPool(extractors);
        this.downloadCompletionService = new ExecutorCompletionService<>(downloadService);
        this.extractCompletionService = new ExecutorCompletionService<>(extractService);
        this.downloader = downloader;
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
        ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        List<String> downloaded = new ArrayList<>();

        Set<String> used = new HashSet<>();
        Queue<List<String>> layer = new ArrayDeque<>();

        Document document = getDocument(url, errors);
        if (document != null) { // toDo
            downloaded.add(url);
            List<String> urls = getLinks(url, document, errors);
            if (urls != null) {
                layer.add(urls);
            }
        }

        while (!layer.isEmpty() && --depth > 0) {
            for (String currentUrl : layer.poll()) {
                downloadCompletionService.submit(() -> getDocument(currentUrl, errors));

                downloadService.

                        Future<Document> poll = downloadCompletionService.take();
                if (poll.get() {

                }
            }

            try {
                while (extractCompletionService)
                    downloadCompletionService.take();
            } catch (InterruptedException e) {

            }
        }

        return new Result(downloaded, errors);
    }

    private List<String> getLinks(String url, Document document, ConcurrentMap<String, IOException> errors) {
        if (document != null) {
            try {
                return document.extractLinks();
            } catch (IOException e) {
                errors.put(url, e);
            }
        }

        return null; // toDo empty?
    }

    private Document getDocument(String url, ConcurrentMap<String, IOException> errors) {
        try {
            return downloader.download(url);
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
}
