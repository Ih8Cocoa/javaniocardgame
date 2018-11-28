package com.meowmeow;

import com.meowmeow.classes.Client;

import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        try (var client = new Client()) {
            // automatically start a new game for the user, which also tests the connection
            client.query("new-user");
        } catch (Exception e) {
            // connection failed - exit the app
            System.out.println("Connection failed - The server was not found. Exiting the app...");
            System.exit(0);
        }

        // then loop until the user loses all the cash, or voluntarily leaves
        while (true) {
            System.out.print("Please enter query: ");
            var scanner = new Scanner(System.in);
            var query = scanner.nextLine();
            // initiate a new connection
            try (var client = new Client()) {
                var response = client.query(query);
                if (query.length() > 9 && query.substring(0,9).equals("quit-game")) {
                    break;
                }
                if (response.contains("You've lost all of the cash")) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
