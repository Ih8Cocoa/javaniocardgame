package com.meowmeow;

public class MainServer {

    public static void main(String[] args) {
        // write your code here
        try (var server = new Server()) {
            server.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
