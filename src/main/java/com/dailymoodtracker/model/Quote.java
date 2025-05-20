package com.dailymoodtracker.model;

public class Quote {
    private final String quoteId;
    private final String moodTypeId;
    private String quoteText;

    public Quote(String quoteId, String moodTypeId, String quoteText) {
        this.quoteId = quoteId;
        this.moodTypeId = moodTypeId;
        this.quoteText = quoteText;
    }

    // Getters
    public String getQuoteId() {
        return quoteId;
    }

    public String getMoodTypeId() {
        return moodTypeId;
    }

    public String getQuoteText() {
        return quoteText;
    }

    // Setters
    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }
} 