package com.meowmeow;

import com.meowmeow.classes.Client;

import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        try (var client = new Client()) {
            while (true) {
                System.out.print("Please enter query: ");
                var scanner = new Scanner(System.in);
                var query = scanner.nextLine();
                client.query(query);
                if (query.length() > 9 && query.substring(0,9).equals("quit-game")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
