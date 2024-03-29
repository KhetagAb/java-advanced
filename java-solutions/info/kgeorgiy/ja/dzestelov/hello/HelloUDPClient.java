package info.kgeorgiy.ja.dzestelov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 200;
    private ExecutorService executorService;
    private InetSocketAddress socketAddress;

    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            socketAddress = new InetSocketAddress(host, port);
            if (socketAddress.isUnresolved()) {
                throw new UDPClientException("Hostname couldn't be resolved into InetAddress");
            }
        } catch (IllegalArgumentException e) {
            throw new UDPClientException("Invalid hostname or invalid port", e);
        }

        executorService = Executors.newFixedThreadPool(threads, HelloThreadFactory.getFactory());

        // :NOTE: for (int i = 0; i < threads; i++) {
//            executorService.submit()
//        }

        Runnable runnable = () -> {
            for (int n = 0; n < requests; n++) {
                String thread = Thread.currentThread().getName();
                String request = (prefix + thread + "_" + n);

                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
                    do {
                        try {
                            socket.send(UDPUtils.getRequestPacket(request, socketAddress));
                            System.out.println("Request: " + request + " sent.");

                            String response = UDPUtils.getResponseString(socket);
                            List<String> numbers = getNumbers(response);
                            if (numbers.size() == 2 && numbers.get(0).equals(thread) && numbers.get(1).equals(String.valueOf(n))) {
                                System.out.println("Received: " + response);
                                break;
                            }
                        } catch (IOException ignored) {
                        }
                    } while (!socket.isClosed() && !Thread.currentThread().isInterrupted());
                } catch (SocketException e) {
                    throw new UDPClientException("Socket could not be opened", e);
                }
            }
        };

        List<Future<?>> collect = IntStream.range(0, threads)
                .mapToObj(n -> executorService.submit(runnable))
                .collect(Collectors.toList());

        UDPClientException exp = null;
        for (Future<?> future : collect) {
            try {
                future.get();
            } catch (InterruptedException ignored) {
                break;
            } catch (ExecutionException e) {
                if (exp == null) {
                    exp = new UDPClientException("Some threads terminated unexpectedly");
                }
                exp.addSuppressed(e.getCause());
            } finally {
                UDPUtils.shutdownExecutorService(executorService);
            }
        }
    }

    private List<String> getNumbers(String str) {
        Matcher m = Pattern.compile("\\d+").matcher(str);
        List<String> result = new ArrayList<>();
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }

    private static class HelloThreadFactory implements ThreadFactory {
        private final AtomicInteger threads = new AtomicInteger(0);

        private static ThreadFactory getFactory() {
            return new HelloThreadFactory();
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.valueOf(threads.getAndIncrement()));
        }
    }
}

