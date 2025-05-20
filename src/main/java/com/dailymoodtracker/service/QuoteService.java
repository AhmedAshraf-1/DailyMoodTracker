package com.dailymoodtracker.service;

public class QuoteService {
    private final String[] quotes = {
        "Every day is a new beginning.",
        "Your attitude determines your direction.",
        "Make today amazing!",
        "Small progress is still progress.",
        "You've got this!",
        "Be the energy you want to attract.",
        "Focus on the good.",
        " fml I want a Wife.",
        "Stay positive, work hard, make it happen."

    };

    public String getRandomQuote() {
        int index = (int) (Math.random() * quotes.length);
        return quotes[index];
    }
} 