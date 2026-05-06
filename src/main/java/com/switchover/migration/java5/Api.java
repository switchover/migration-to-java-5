package com.switchover.migration.java5;

import com.switchover.migration.java5.converter.Converter;

import java.io.File;

public class Api {
    public String version() {
        return Main.VERSION;
    }

    public void process(String targetPath) {
        if (targetPath == null || targetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("targetPath must not be null or empty");
        }

        process(new File(targetPath));
    }

    public void process(File targetDir) {
        if (targetDir == null) {
            throw new IllegalArgumentException("targetDir must not be null");
        }

        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Target directory does not exist: " + targetDir.getAbsolutePath());
        }

        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory: " + targetDir.getAbsolutePath());
        }

        Converter converter = new Converter(targetDir.getAbsolutePath());
        converter.process();
    }
}
