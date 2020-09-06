package com.github.ericliucn.invfly.exception;

public class DeserializeException extends Exception {

    public DeserializeException(String id){
        super("Failed to deserialize data for " + id);
    }
}
