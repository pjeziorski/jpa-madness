package com.xpj.madness.jpa.services;

import lombok.SneakyThrows;

import java.util.concurrent.*;
import java.util.function.Function;

public class ControllableOperation<R> {

    private final ExecutorService executor;

    private final Function<ControllableOperation<R>, R> operation;

    private AwaitResult<?> currentAwait;
    private CountDownLatch operationResultLock = new CountDownLatch(1);
    private R operationResult;
    private Throwable operationException;

    public ControllableOperation(String operationName,
                                 Function<ControllableOperation<R>, R> operation) {
        this.executor = Executors.newSingleThreadExecutor(NamedThreadFactory.create(operationName));
        this.operation = operation;
    }

    public ControllableOperation<R> start() {
        executor.submit(() -> {
            try {
                operationResult = operation.apply(this);
            }
            catch (Throwable e) {
                operationException = e;
            }
            finally {
                operationResultLock.countDown();
            }
        });
        awaitOperationAction();

        return this;
    }

    // use only inside operation
    public <T> T pauseBefore(String subOperationName, Callable<T> subOperation) {
        AwaitResult<T> await = new AwaitResult<>(subOperationName, subOperation);
        currentAwait = await;
        return await.pause();
    }

    public boolean isPaused() {
        return currentAwait != null;
    }

    public Object resume() {
        AwaitResult await = currentAwait;
        currentAwait = null;
        Object result = await.resume();

        awaitOperationAction();

        return result;
    }

    public Future<Object> resumeAsync() {
        AwaitResult await = currentAwait;
        currentAwait = null;
        return await.resumeAsync();
    }

    @SneakyThrows
    public R complete() {
        while (isPaused()) {
            resume();
        }
        if (operationException != null) {
            throw operationException;
        }

        return operationResult;
    }

    private void awaitOperationAction() {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (currentAwait != null || operationResultLock.getCount() == 0) {
                return;
            }
        }
        throw new RuntimeException("Timeout on waiting for operation action");
    }

    static class AwaitResult<SubOperationResult> {
        private final String subOperationName;
        private final Callable<SubOperationResult> subOperation;

        private final CountDownLatch subOperationLock = new CountDownLatch(1);
        private final CountDownLatch resultLock = new CountDownLatch(1);

        private boolean shouldThrowRuntimeException = false;
        private boolean shouldThrowException = false;

        private SubOperationResult result;

        private Throwable resultException;

        public AwaitResult(
                String subOperationName,
                Callable<SubOperationResult> subOperation) {
            this.subOperationName = subOperationName;
            this.subOperation = subOperation;
        }

        @SneakyThrows
        public SubOperationResult pause() {
            log("before " + subOperationName);
            subOperationLock.await();

            log("resume " + subOperationName);
            try {
                if (shouldThrowRuntimeException) {
                    throw new RuntimeException();
                }
                if (shouldThrowException) {
                    throw new Exception("Non Runtime Exception");
                }
                result = subOperation.call();

                return result;
            }
            catch (Throwable e) {
                resultException = e;
                e.printStackTrace();
                throw e;
            }
            finally {
                log("completed " + subOperationName);
                resultLock.countDown();
            }
        }

        @SneakyThrows
        public SubOperationResult resume() {
            subOperationLock.countDown();
            resultLock.await();

            if (resultException != null) {
                throw resultException;
            }
            return result;
        }

        public Future<SubOperationResult> resumeAsync() {
            return Executors.newSingleThreadExecutor().submit(() -> resume());
        }

        private void log(String message) {
            System.err.println(Thread.currentThread().getName() + " " + message);
        }

    }
}
