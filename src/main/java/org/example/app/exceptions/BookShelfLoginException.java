package org.example.app.exceptions;

public class BookShelfLoginException extends Exception {

    private final String message;

    public BookShelfLoginException(String s) {
        this.message = s;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
