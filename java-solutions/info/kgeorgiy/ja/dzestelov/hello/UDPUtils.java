package info.kgeorgiy.ja.dzestelov.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class UDPUtils {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    static DatagramPacket getRequestPacket(String request, SocketAddress socketAddress) {
        return getRequestPacket(request, socketAddress, DEFAULT_CHARSET);
    }

    static DatagramPacket getRequestPacket(String request, SocketAddress socketAddress, Charset charset) {
        byte[] requestBuffer = request.getBytes(charset);
        return new DatagramPacket(requestBuffer, requestBuffer.length, socketAddress);
    }

    static DatagramPacket getResponsePacket(DatagramSocket socket) throws IOException {
        int maxSize = socket.getReceiveBufferSize();
        DatagramPacket receive = new DatagramPacket(new byte[maxSize], maxSize);
        socket.receive(receive);
        return receive;
    }

    static String getResponseString(DatagramSocket socket) throws IOException {
        return getResponseString(socket, DEFAULT_CHARSET);
    }

    static String getResponseString(DatagramSocket socket, Charset charset) throws IOException {
        DatagramPacket packet = getResponsePacket(socket);
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), charset);
    }
}
