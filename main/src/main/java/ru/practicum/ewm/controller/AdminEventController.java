package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.dto.EventDtoFull;
import ru.practicum.ewm.model.dto.EventUpdateDto;
import ru.practicum.ewm.exception.ParameterException;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDtoFull> searchEventsByAdmin(@RequestParam Long[] users,
                                                  @RequestParam String[] states,
                                                  @RequestParam Long[] categories,
                                                  @RequestParam String rangeStart,
                                                  @RequestParam String rangeEnd,
                                                  @RequestParam(required = false, defaultValue = "0") int from,
                                                  @RequestParam(required = false, defaultValue = "10") int size) {
        var foundedEvents = eventService.searchEventsAdmin(
                users,
                parseEnum(states),
                categories,
                parseTime(rangeStart),
                parseTime(rangeEnd),
                from,
                size
        );
        log.info("[GET /admin/events?users={users}&states={states}&categories={categories}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&from={from}&size={size}] (Admin). " +
                        "Search list events (model) with param users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return foundedEvents;
    }

    @PatchMapping("/{eventId}")
    private EventDtoFull updateEventByAdmin(@PathVariable Long eventId,
                                            @RequestBody EventUpdateDto eventDto) {
        var updatedEventByAdmin = eventService.updateEventByAdmin(eventId, eventDto);
        log.info("[PATCH /admin/events/{eventId}] (Admin). Update event (id): {} to event (dto): {}", eventId, eventDto);
        return updatedEventByAdmin;
    }

    private LocalDateTime parseTime(String dateTime) {
        return LocalDateTime.parse(dateTime, formatter);
    }

    private EventState[] parseEnum(String[] states) {
        EventState[] result = new EventState[states.length];
        for (int i = 0; i < states.length; i++) {
            try {
                result[i] = EventState.valueOf(states[i]);
            } catch (IllegalArgumentException e) {
                throw new ParameterException("Unknown state: " + states[i]);
            }
        }
        return result;
    }
}
