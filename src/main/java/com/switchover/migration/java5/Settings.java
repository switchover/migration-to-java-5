package com.switchover.migration.java5;

public final class Settings {
    private static final int DEFAULT_THREAD_POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors());

    private final int threadPoolSize;

    private Settings(Builder builder) {
        this.threadPoolSize = builder.threadPoolSize;
    }

    public static Settings defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public static final class Builder {
        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

        private Builder() {
        }

        public Builder threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public Settings build() {
            if (threadPoolSize < 1) {
                throw new IllegalArgumentException("threadPoolSize must be greater than 0");
            }

            return new Settings(this);
        }
    }
}
