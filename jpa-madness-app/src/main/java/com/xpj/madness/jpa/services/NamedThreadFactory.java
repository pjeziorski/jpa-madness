package com.xpj.madness.jpa.services;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory {

    private static final AtomicInteger threadCount = new AtomicInteger(0);

    public static ThreadFactory create(String name) {
        return (runnable) -> new Thread(runnable, "th" + threadCount.incrementAndGet() + "-" + name);
    }
}
