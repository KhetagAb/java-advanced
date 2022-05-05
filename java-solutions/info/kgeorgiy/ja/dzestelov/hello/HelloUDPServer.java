package info.kgeorgiy.ja.dzestelov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Class represents simple UDP server
 */
public class HelloUDPServer implements HelloServer {

    private static final int SOCKET_TIMEOUT = 1000;

    private boolean isStarted = false;
    private DatagramSocket socket;
    private ExecutorService workers;

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

        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            throw new UDPClientException("Cannot create socket", e);
        }

        workers = Executors.newFixedThreadPool(threads);

        IntStream.range(0, threads)
                .forEach(n -> workers.submit(() -> {
                    while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        try {
                            DatagramPacket request = UDPUtils.getResponsePacket(socket);
                            String requestString = UDPUtils.getResponseString(request);

                            DatagramPacket packet = UDPUtils.getRequestPacket("Hello, " + requestString, request.getSocketAddress());
                            socket.send(packet);
                        } catch (IOException ignored) {
                        }
                    }
                }));
    }
    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        workers.shutdownNow();
    }
}
