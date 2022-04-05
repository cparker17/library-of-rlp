package com.parker.rlp.exceptions.book;

public class NoSuchBookException extends BookException{
    public NoSuchBookException(String message) {
        super(message);
    }
}
