package com.github.ericliucn.invfly.exception;

public class SerializeException extends Exception{
    public SerializeException(String id){
        super("Failed to serialize data for " + id);
    }
}
