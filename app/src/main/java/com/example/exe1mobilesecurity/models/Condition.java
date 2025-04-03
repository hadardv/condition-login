package com.example.exe1mobilesecurity.models;

public class Condition {
    private final String description;
    private boolean isMet;

    public Condition(String description, boolean isMet) {
        this.description = description;
        this.isMet = isMet;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMet() {
        return isMet;
    }

    public void setIsMet(boolean met)
    {
        isMet = met;
    }

}
