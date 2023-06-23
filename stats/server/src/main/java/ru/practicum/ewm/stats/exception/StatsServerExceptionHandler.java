package ru.practicum.ewm.stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class StatsServerExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public StatsErrorResponse handleParameter(final StatsParameterException e) {
        log.error("Error processing request: {}.", e.getMessage());
        return new StatsErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public StatsErrorResponse handleNotFoundStats(final StatsNotFoundException e) {
        log.error("Error processing request: {}.", e.getMessage());
        return new StatsErrorResponse(e.getMessage());
    }
}
