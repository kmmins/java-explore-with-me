package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.dto.EventDto;
import ru.practicum.ewm.model.dto.EventDtoFull;
import ru.practicum.ewm.model.EventModel;
import ru.practicum.ewm.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class EventConverter {

    public static EventModel convToModel(UserModel user, EventDto dto) {
        EventModel model = new EventModel();
        model.setTitle(dto.getTitle());
        model.setDescription(dto.getDescription());
        model.setAnnotation(dto.getAnnotation());
        model.setInitiator(user);
        model.setLocation(dto.getLocation());
        model.setEventDate(dto.getEventDate());
        model.setParticipantLimit(dto.getParticipantLimit());
        model.setRequestModeration(dto.getRequestModeration());
        model.setPaid(dto.getPaid());

        return model;
    }

    public static EventDto convToDto(EventModel model) {
        EventDto dto = new EventDto();
        dto.setTitle(model.getTitle());
        dto.setDescription(model.getDescription());
        dto.setAnnotation(model.getAnnotation());
        dto.setLocation(model.getLocation());
        dto.setEventDate(model.getEventDate());
        dto.setParticipantLimit(model.getParticipantLimit());
        dto.setRequestModeration(model.getRequestModeration());
        dto.setPaid(model.getPaid());

        return dto;
    }

    public static List<EventDto> mapToDto(List<EventModel> events) {
        List<EventDto> res = new ArrayList<>();
        for (EventModel e : events) {
            res.add(convToDto(e));
        }
        return res;
    }

    public static EventDtoFull convToDtoFull(EventModel model) {
        return new EventDtoFull(
                model.getTitle(),
                model.getDescription(),
                model.getAnnotation(),
                model.getCategory(),
                model.getInitiator(),
                model.getLocation(),
                model.getEventDate(),
                model.getCreatedOn(),
                model.getPublishedOn(),
                model.getParticipantLimit(),
                model.countConfirmedRequests(),
                model.getRequestModeration(),
                model.getPaid(),
                model.getState(),
                null
        );
    }

    public static List<EventDtoFull> mapToDtoFull(List<EventModel> events) {
        List<EventDtoFull> resFull = new ArrayList<>();
        for (EventModel e : events) {
            resFull.add(convToDtoFull(e));
        }
        return resFull;
    }
}
