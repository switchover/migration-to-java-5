package com.switchover.migration.java5;

import com.switchover.migration.java5.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Api {

    private final Logger logger = LoggerFactory.getLogger(Api.class);

    public String version() {
        return Main.VERSION;
    }

    public void process(String targetPath) {
        process(targetPath, Settings.defaults());
    }

    public void process(String targetPath, Settings settings) {
        if (targetPath == null || targetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("targetPath must not be null or empty");
        }

        process(new File(targetPath), settings);
    }

    public void process(File targetDir) {
        process(targetDir, Settings.defaults());
    }

    public void process(File targetDir, Settings settings) {
        if (targetDir == null) {
            throw new IllegalArgumentException("targetDir must not be null");
        }

        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null");
        }

        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Target directory does not exist: " + targetDir.getAbsolutePath());
        }

        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory: " + targetDir.getAbsolutePath());
        }

        logger.info("Thread pool size: {}", settings.getThreadPoolSize());
        Converter converter = new Converter(targetDir.getAbsolutePath(), settings.getThreadPoolSize());
        converter.process();
    }
}
