package ru.practicum.ewm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.CategoryModel;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.LocationModel;
import ru.practicum.ewm.model.UserModel;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDtoFull {

    private Long id;
    private String title;
    private String description;
    private String annotation;
    private CategoryModel category;
    private UserModel initiator;
    private LocationModel location;
    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    private Integer participantLimit;
    private Long confirmedRequests;
    private Boolean requestModeration;
    private Boolean paid;
    private EventState state;
    private Long views;
}
