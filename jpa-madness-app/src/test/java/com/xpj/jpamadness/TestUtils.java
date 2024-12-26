package com.xpj.jpamadness;

public class TestUtils {

    public static void printHash(String name, Object obj) {
        System.err.println(name + ": " + System.identityHashCode(obj));
    }

}
