package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.exception.ParameterException;
import ru.practicum.ewm.model.EventSort;
import ru.practicum.ewm.model.dto.EventDtoFull;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Validated
@RestControllerAdvice
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventDtoFull> getEventsPublic(@RequestParam String text,
                                              @RequestParam Long[] categories,
                                              @RequestParam boolean paid,
                                              @RequestParam String rangeStart,
                                              @RequestParam String rangeEnd,
                                              @RequestParam(required = false) Boolean onlyAvailable,
                                              @RequestParam String sort,
                                              @RequestParam(required = false, defaultValue = "0") int from,
                                              @RequestParam(required = false, defaultValue = "10") int size,
                                              HttpServletRequest request) {
        var events = eventService.getEventsPublic(
                text,
                categories,
                paid,
                parseTime(rangeStart),
                parseTime(rangeEnd),
                onlyAvailable,
                parseEnum(sort),
                from,
                size,
                request);
        log.info("[GET /events?pinned={pinned}&from={from}&size={size}] (Public). " +
                        "Get events with params text: {}, categories: {}, paid: {}, rangeStart: {}, rangeEnd: {}, onlyAvailable: {}, sort: {}, from: {}, size: {}, from client ip: {}, endpoint path: {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request.getRemoteAddr(), request.getRequestURI());
        return events;
    }

    @GetMapping("/{id}")
    public EventDtoFull getEventByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        var eventById = eventService.getEventByIdPublic(id, request);
        log.info("[GET /events/{id}] (Public). Get event (id): {}, from client ip: {}, endpoint path: {}",
                id, request.getRemoteAddr(), request.getRequestURI());
        return eventById;
    }

    private LocalDateTime parseTime(String dateTime) {
        return LocalDateTime.parse(dateTime, formatter);
    }

    private EventSort parseEnum(String s) {
        EventSort e;
        try {
            e = EventSort.valueOf(s);
        } catch (IllegalArgumentException exc) {
            throw new ParameterException("Unknown state: " + s);
        }
        return e;
    }
}
