package com.meowmeow.classes;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>Java NIO Game Server Class</h3>
 * <p>This class contains all of the data necessary to operate the server</p>
 * <br />
 * <p>
 *     This server object implements {@link AutoCloseable}.
 *     Therefore, it is recommended to use the try-with-resources block to ensure that the server shuts down properly(?)
 * </p>
 * <br />
 * <p>The server operate as follows:</p>
 * <ul>
 *     <li>Once the client connects to this server, the server will accept the request and maintain the connection</li>
 *     <li>The client can send queries to the server which the server can handle. The valid query formats are:
 *     <ul>
 *         <li>"new-user" - generate a new user server-side and send the user-id to the client.
 *         That user will have 1000000 cash by default
 *         </li>
 *         <li>"new-game user-id <i>your-user-id</i> bet-money <i>your-bet-money</i>" - initiate a new game
 *         and update the the user's cash server-side
 *         </li>
 *         <li>"delete-user user-id <i>your-user-id</i>" - delete the user data from the server</li>
 *         <li>Any queries not conforming to the above specification will throw an {@link IllegalGameQueryException},
 *         which will be handled internally on the server</li>
 *     </ul>
 *     </li>
 *     <li>The server can process the query and send a string to the client representing the result</li>
 * </ul>
 * <p>The server's buffer is a {@link ConcurrentHashMap}, storing the user ID and their corresponding amount of cash</p>
 *
 * @author Ih8Cocoa
 */
public class Server implements AutoCloseable {

    private ConcurrentHashMap<UUID, Integer> serverData = new ConcurrentHashMap<>();

    private ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

    private ServerSocket serverSocket = serverSocketChannel.socket();

    private Selector selector = Selector.open();

    /**
     * <p>- Mommy can you hire me?</p>
     * <p>- To write good and readable {@code code}?</p>
     * <p>- yeeees?</p>
     * <p>
     *     (actually write all of the shit code in a single file and reference it in the {@code main()} like a boss)
     * </p>
     * <h1>ABSTRACTION TIME!!!</h1>
     * @throws IOException An IOException to be handled in main
     */
    public Server() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(12345));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void close() throws Exception {
        selector.close();
        serverSocketChannel.close();
        serverSocket.close();
    }

    /**
     * <p>Starts the server to process the queries.</p>
     *
     * <p>The client connection lifecycle within the server is as follows:</p>
     * <ul>
     *     <li>Accepts the connection</li>
     *     <li>Read, processes the query from the connection, and send back a response</li>
     *     <li>Terminate the connection</li>
     * </ul>
     *
     * @throws IOException to be handled in main
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void startServer() throws IOException {
        while (true) {
            // server is ready
            System.out.println("Server is waiting for events...");
            var select = selector.select();
            System.out.printf("Received %d events\n", select);

            //get all the keys from selector, and loop through each of them
            var keys = selector.selectedKeys();
            for (var key : keys) {
                if (key.isAcceptable()) {
                    acceptRequest(key);
                    keys.remove(key);
                } else if (key.isReadable()) {
                    processRequest(key);
                    keys.remove(key);
                    //cancel the key after read operation
                    key.cancel();
                }
            }
        }
    }

    /**
     * Accepting a request from a new client. After that, register the "ready-to-read" operation to the selector
     * @param key to extract the {@link ServerSocketChannel}
     * @throws IOException bounce this exception to main
     */
    private void acceptRequest(@NotNull SelectionKey key) throws IOException {
        System.out.println("Accepting the request");
        var channel = (ServerSocketChannel) key.channel();
        var socketChannel = channel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * <p>
     *     Process a query from the client. Haven't implemented a query buffer yet, so the query has to be
     *     resolved immediately.
     * </p>
     * @param key to extract the {@link SocketChannel}
     * @throws IOException bounce this exception to main
     */
    private void processRequest(@NotNull SelectionKey key) throws IOException {
        // get socket channel and prepare the reading buffer
        var socketChannel = (SocketChannel) key.channel();
        var byteBuffer = ByteBuffer.allocate(1048576);

        // read and decode the user query
        System.out.println("Reading the request");
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        var charBuffer = StandardCharsets.ISO_8859_1.decode(byteBuffer);
        var query = new String(charBuffer.array()).toLowerCase();
        System.out.println("Read query: " + query);

        // process the query
        var serverOutput = "";
        try {
            serverOutput = gameSession(query);
        } catch (IllegalGameQueryException e) {
            serverOutput = e.getMessage();
        }

        System.out.println("Sending to client: " + serverOutput + "\n");

        // send output to client
        byteBuffer = StandardCharsets.ISO_8859_1.encode(serverOutput);
        byteBuffer.compact();
        byteBuffer.flip();
        // bug fix: If the client quits, catch Broken Pipe IOException to cancel the key
        try {
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            System.out.println("A client has disconnected from the server\n");
            key.cancel();
        }

    }

    /**
     * Executes the query sent from the client
     *
     * @param query the user's query
     * @return a string representing the result
     * @throws IllegalGameQueryException representing an invalid query
     */
    @NotNull
    private String gameSession(@NotNull String query) throws IllegalGameQueryException {
        final int START_MONEY = 1000000;

        // if new-user -> make a new user in the server buffer
        if (query.equals("new-user")) {
            System.out.println("Creating a new user...");
            var newId = UUID.randomUUID();
            this.serverData.put(newId, START_MONEY);
            return "user-id " + newId + " amount " + START_MONEY;
        }

        // the query uses an existing user - split the queries
        var queryFragments = query.split(" ");
        UUID userId;

        //try parsing the user ID
        try {
            userId = UUID.fromString(queryFragments[2]);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalGameQueryException();
        }

        // if the user is not found -> invalid query
        if (!this.serverData.containsKey(userId)) {
            throw new IllegalGameQueryException(userId);
        }

        //initiate a new game?
        if (query.substring(0, 17).equals("new-game user-id ") && queryFragments[3].equals("bet-money")) {
            int betMoney;
            try {
                betMoney = Integer.parseInt(queryFragments[4]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalGameQueryException();
            }
            return gameResult(userId, betMoney);
        }

        // quit a game -> remove the user ID from the buffer
        if (query.substring(0, 18).equals("quit-game user-id ")) {
            serverData.remove(userId);
            return "User ID " + userId + " have quit. Have a nice day!";
        }

        //the query is probably gibberish. Exception time!
        throw new IllegalGameQueryException();
    }

    /**
     * Initiate a new game and modify the server's buffer accordingly
     *
     * @param userId the user's {@link UUID} value
     * @param betMoney the amount of cash that the user has bet
     * @return a new string representing the result of the game
     */
    @NotNull
    private String gameResult(final UUID userId, final int betMoney) {
        //prepare stuff
        var rtn = new StringBuilder();
        var drawnCards = new ArrayList<Card>();
        var clientScore = new Score();
        var serverScore = new Score();

        // draw 6 cards
        for (int i = 0; i < 6; i++) {
            //ensure that all cards are unique
            var card = new Card();
            while (drawnCards.contains(card)) {
                card = new Card();
            }
            drawnCards.add(card);

            // draw cards sequentially: client -> server -> client -> ...
            if (i % 2 == 0) {
                clientScore.setPoint(card);
                var str = "Client card " + card.toString() + "\n";
                rtn.append(str);
            } else {
                serverScore.setPoint(card);
                var str = "Server card " + card.toString() + "\n";
                rtn.append(str);
            }
        }

        rtn.append("Server point: ").append(serverScore.getPoint()).append("\nYour point: ")
                .append(clientScore.getPoint()).append("\n");

        //determine who won the game
        var winState = clientScore.compareTo(serverScore);
        if (winState > 0) {
            clientWinProtocol(userId, betMoney, rtn);
        } else if (winState < 0) {
            serverWinProtocol(userId, betMoney, rtn);
        } else {
            rtn.append("Draw! Your current money is ").append(serverData.get(userId)).append("\n");
        }
        return rtn.toString();
    }

    /**
     * The client won so add the money to the correct user
     * @param userId the user ID
     * @param betMoney the amount of cash to add
     * @param rtn from the parent method
     */
    private void clientWinProtocol(final UUID userId, final int betMoney, @NotNull StringBuilder rtn) {
        //in the case of client winning
        serverData.computeIfPresent(userId, (k,v) -> v + betMoney);
        rtn.append("You won! Your current money is ").append(serverData.get(userId)).append("\n");
    }

    /**
     * The server won so deduct the money from the correct user
     * @param userId the user ID
     * @param betMoney the amount of cash to remove
     * @param rtn from the parent method
     */
    private void serverWinProtocol(final UUID userId, final int betMoney, @NotNull StringBuilder rtn) {
        //in the case of client losing
        serverData.computeIfPresent(userId, (k,v) -> v - betMoney);
        var cash = serverData.get(userId);
        rtn.append("You lost! Your current money is ").append(cash).append("\n");

        //if the amount of cash is negative -> kicks the user out
        if (cash < 1) {
            rtn.append("You've lost all of the cash. Come back next time.");
            serverData.remove(userId);
        }
    }
}
