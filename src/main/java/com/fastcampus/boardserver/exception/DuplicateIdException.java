package com.fastcampus.boardserver.exception;

public class DuplicateIdException extends RuntimeException {
    public DuplicateIdException(String s) {
        super(s);
    }
}
