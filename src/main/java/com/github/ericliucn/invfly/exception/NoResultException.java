package com.github.ericliucn.invfly.exception;

import java.util.UUID;

public class NoResultException extends Exception{
    public NoResultException(UUID uuid){
        super("Can't find the result of task " + uuid.toString() + ", plugin may malfunction");
    }
}
