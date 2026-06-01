package com.switchover.migration.java5;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.File;
import java.io.IOException;

public class Main {
    public static final String VERSION = "v0.1 - Migration to Java 5";

    public static void main(String[] args) {
        Options options = new Options();

        Option targetDir = Option.builder("t")
            .longOpt("target")
            .argName("Dir.")
            .hasArg()
            .desc("Target source directory")
            .get();

        Option verbose = Option.builder("v")
            .longOpt("verbose")
            .desc("Verbose output")
            .get();

        Option threads = Option.builder("p")
            .longOpt("threads")
            .argName("Count")
            .hasArg()
            .desc("Thread pool size")
            .get();

        Option version = Option.builder("V")
            .longOpt("version")
            .desc("Show version information")
            .get();

        Option help = Option.builder("h")
            .longOpt("help")
            .desc("Display help information")
            .get();

        options.addOption(targetDir);
        options.addOption(verbose);
        options.addOption(threads);
        options.addOption(version);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelp(options);
                return;
            }

            if (cmd.hasOption("V")) {
                System.out.println(VERSION);
                return;
            }

            if (cmd.hasOption("v")) {
                System.out.println("Verbose mode enabled");
            }

            if (!cmd.hasOption("t")) {
                System.out.println("Target directory is required");
                printHelp(options);
                return;
            }

            String target = cmd.getOptionValue("t");
            try {
                System.out.println("Target directory: " + new File(target).getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Settings.Builder settingsBuilder = Settings.builder();
            if (cmd.hasOption("p")) {
                try {
                    settingsBuilder.threadPoolSize(Integer.parseInt(cmd.getOptionValue("p")));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Thread pool size must be a positive integer", e);
                }
            }

            Api api = new Api();
            api.process(target, settingsBuilder.build());

        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            printHelp(options); // Print help if parsing fails
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            printHelp(options);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = HelpFormatter.builder()
            .setShowSince(false)
            .get();
        try {
            formatter.printHelp("java -jar migration-to-java-5.jar",
                "", options, "", false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
