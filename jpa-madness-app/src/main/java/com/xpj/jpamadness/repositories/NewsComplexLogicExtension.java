package com.xpj.jpamadness.repositories;

import java.util.concurrent.atomic.AtomicBoolean;

public interface NewsComplexLogicExtension {

    AtomicBoolean shouldThrowRuntimeException = new AtomicBoolean(false);

    void transactionWithSave();

    void transactionWithSaveAndFlush();

    void privateTransactionWithSaveAndFlush();
}
