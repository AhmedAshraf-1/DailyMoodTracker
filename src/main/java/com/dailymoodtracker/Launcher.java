package com.dailymoodtracker;

/**
 * Non-modular entry point for the application.
 * This class simply delegates to the modular MainApp, providing
 * a convenient way to start the application in a non-modular environment.
 */
public class Launcher {
    
    /**
     * Main method that simply delegates to the modular MainApp.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
} 