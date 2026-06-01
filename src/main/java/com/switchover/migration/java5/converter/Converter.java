package com.switchover.migration.java5.converter;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Converter {
    private static final String RULES_PACKAGE = "com.switchover.migration.java5.converter.rules";
    private static final List<Class<? extends Rule>> RULE_TYPES = loadRuleTypes();

    private final Logger logger = LoggerFactory.getLogger(Converter.class);

    private final String target;
    private final int threadPoolSize;

    public Converter(String target) {
        this(target, Math.max(1, Runtime.getRuntime().availableProcessors()));
    }

    public Converter(String target, int threadPoolSize) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("threadPoolSize must be greater than 0");
        }

        this.target = target;
        this.threadPoolSize = threadPoolSize;
    }

    public void process() {
        File targetDir = new File(target);

        Collection<File> files = FileUtils.listFiles(targetDir, new String[]{"java", "jav"}, true);

        if (files.isEmpty()) {
            logger.info("No Java source files found in {}", targetDir.getAbsolutePath());
            return;
        }

        logger.info("Processing {} files with thread pool size {}", files.size(), threadPoolSize);

        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (File file : files) {
                futures.add(executorService.submit(() -> processFile(file)));
            }

            int modifiedCount = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    modifiedCount++;
                }
            }

            logger.info("Completed processing {} files. Modified {} files.", files.size(), modifiedCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("File conversion interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to process source files", e.getCause());
        } finally {
            shutdownExecutor(executorService);
        }
    }

    private boolean processFile(File file) {
        logger.info("Processing file: {}", file.getAbsolutePath());

        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_1_4);
        JavaParser parser = new JavaParser(configuration);

        try {
            return parser.parse(file).getResult().map(cu -> {
                LexicalPreservingPrinter.setup(cu);

                boolean modified = false;
                for (Class<? extends Rule> clazz : RULE_TYPES) {
                    try {
                        Rule instance = clazz.getDeclaredConstructor().newInstance();
                        logger.info("Applying rule: {}", clazz.getSimpleName());
                        if (instance.apply(cu)) {
                            modified = true;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to apply rule: {}", clazz.getCanonicalName(), e);
                    }
                }

                if (modified) {
                    String newCode = LexicalPreservingPrinter.print(cu);
                    try {
                        FileUtils.writeStringToFile(file, newCode, StandardCharsets.UTF_8);
                        logger.info(" - modified");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    logger.info(" - skipped");
                }

                return modified;
            }).orElseGet(() -> {
                logger.warn("Unable to parse file: {}", file.getAbsolutePath());
                return false;
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Class<? extends Rule>> loadRuleTypes() {
        Reflections reflections = new Reflections(RULES_PACKAGE,
            Scanners.SubTypes.filterResultsBy(c -> true));

        return reflections.getSubTypesOf(Rule.class).stream()
            .sorted(Comparator.comparing(Class::getName))
            .toList();
    }

    private void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
