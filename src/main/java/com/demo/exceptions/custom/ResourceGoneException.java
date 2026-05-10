package com.demo.exceptions.custom;

public class ResourceGoneException extends RuntimeException{
    public ResourceGoneException(String message) {
        super(message);
    }
}
