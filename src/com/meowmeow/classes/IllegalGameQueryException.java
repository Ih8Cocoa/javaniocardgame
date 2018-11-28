package com.meowmeow.classes;

import java.util.UUID;

/**
 * If this is thrown, it means that the client has submitted an invalid query
 */
public class IllegalGameQueryException extends Exception {
    public IllegalGameQueryException() {
        super("Invalid game query format, please try again");
    }

    public IllegalGameQueryException(UUID id) {
        super("User ID " + id.toString() + " does not exist, please create the user first");
    }
}
