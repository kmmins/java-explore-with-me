package ru.practicum.ewm.stats.exception;

public class StatsErrorResponse {

    private final String error;

    public StatsErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
