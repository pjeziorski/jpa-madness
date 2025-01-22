package com.xpj.madness.jpa;

public class RuntimeExceptionWrapper extends RuntimeException {

    public RuntimeExceptionWrapper(String msg, Throwable e) {
        super(msg, e);
    }
}
