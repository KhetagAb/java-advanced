package info.kgeorgiy.ja.dzestelov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 100;
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

        Runnable runnable = () -> {
            for (int n = 0; n < requests; n++) {
                String request = (prefix + Thread.currentThread().getName() + "_" + n);

                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
                    do {
                        try {
                            socket.send(UDPUtils.getRequestPacket(request, socketAddress));
                            System.out.println("Request: " + request + " sent.");

                            String response = UDPUtils.getResponseString(socket);
                            if (response.contains(request)) {
                                System.out.println("Receive: " + response);
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
                close();
            }
        }
    }

    private void close() {
        UDPUtils.shutdownExecutorService(executorService);
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

