package com.xpj.madness.jpa.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class AdHocTransaction {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public <T> T readCommitted(Supplier<T> operation) {
        return operation.get();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void readCommitted(Runnable operation) {
        operation.run();
    }
}
