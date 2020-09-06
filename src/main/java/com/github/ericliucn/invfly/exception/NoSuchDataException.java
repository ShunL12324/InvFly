package com.github.ericliucn.invfly.exception;

public class NoSuchDataException extends Exception{
    public NoSuchDataException(String id){
        super("No data present for " + id);
    }
}
