package com.meowmeow.classes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * The client. Which is {@link AutoCloseable} just like the server
 */
public class Client implements AutoCloseable {
    private SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 12345));

    public Client() throws IOException {}

    @Override
    public void close() throws Exception {
        socketChannel.close();
    }

    /**
     * Send the query to the server, get its output and print it out to the screen
     * @param query the query from the user
     * @throws IOException something throws it idk, just pass it to main
     */
    public String query(String query) throws IOException {
        //encode query and send to server
        var buffer = StandardCharsets.ISO_8859_1.encode(query);
        socketChannel.write(buffer);

        //receive response from server and decode to string
        buffer = ByteBuffer.allocate(1048576);
        socketChannel.read(buffer);
        buffer.flip();
        var charBuffer = StandardCharsets.ISO_8859_1.decode(buffer);
        var response = new String(charBuffer.array()).trim();

        //write the response to console
        System.out.println(response);
        return response;
    }
}
