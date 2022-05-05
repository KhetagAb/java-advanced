package info.kgeorgiy.ja.dzestelov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Class represents simple UDP server
 */
public class HelloUDPServer implements HelloServer {

    private boolean isStarted = false;
    private ExecutorService server;
    private ExecutorService responses;

    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        if (isStarted) {
            return;
        } else {
            isStarted = true;
        }

        responses = new ThreadPoolExecutor(threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadPoolExecutor.DiscardPolicy());
        server = Executors.newSingleThreadExecutor();

        server.submit(() -> {
            try (DatagramSocket datagramSocket = new DatagramSocket(port)) {
                do {
                    try {
                        DatagramPacket request = UDPUtils.getResponsePacket(datagramSocket);
                        String responseString = UDPUtils.getResponseString(request);
                        responses.submit(() -> {
                            DatagramPacket packet = UDPUtils.getRequestPacket("Hello, " + responseString, request.getSocketAddress());
                            try {
                                datagramSocket.send(packet);
                            } catch (IOException ignored) {
                            }
                        });
                    } catch (IOException e) {
                        System.out.println("Cannot get response packet: " + e.getMessage());
                    }
                } while (!datagramSocket.isClosed() && !Thread.currentThread().isInterrupted());
            } catch (SocketException e) {
                throw new UDPClientException("Socket cannot be opened", e);
            }
        });
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        UDPUtils.shutdownExecutorService(server);
        UDPUtils.shutdownExecutorService(responses);
    }
}
