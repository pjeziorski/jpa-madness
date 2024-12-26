package com.xpj.madness.jpa;

public class TestUtils {

    public static void printHash(String name, Object obj) {
        System.err.println(name + ": " + System.identityHashCode(obj));
    }

}
