package com.resumeanalyzer;

/**
 * Standard Java entry point without extending Application directly.
 * Allows executing the JavaFX application directly from fat JARs or standard classloaders.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
