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
import java.util.Iterator;

public class Converter {
    private static final String RULES_PACKAGE = "com.switchover.migration.java5.converter.rules";

    private final Logger logger = LoggerFactory.getLogger(Converter.class);

    private final String target;

    public Converter(String target) {
        this.target = target;
    }

    public void process() {
        File targetDir = new File(target);

        Iterator<File> fileIterator = FileUtils.iterateFiles(targetDir, new String[]{"java", "jav"}, true);

        while (fileIterator.hasNext()) {
            File file = fileIterator.next();

            logger.info("Processing file: {}", file.getAbsolutePath());

            ParserConfiguration configuration = new ParserConfiguration();
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_1_4);
            JavaParser parser = new JavaParser(configuration);

            try {
                parser.parse(file).getResult().ifPresent(cu -> {
                    // 원본 포맷(주석, 줄바꿈 등)을 유지
                    LexicalPreservingPrinter.setup(cu);

                    Reflections reflections = new Reflections(RULES_PACKAGE,
                        Scanners.SubTypes.filterResultsBy(c -> true));

                    boolean modified = false;
                    for (Class<? extends Rule> clazz : reflections.getSubTypesOf(Rule.class)) {
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
                            FileUtils.writeStringToFile(file, newCode, "UTF-8");
                            logger.info(" - modified");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        logger.info(" - skipped");
                    }
                });
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
