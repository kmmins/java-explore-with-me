package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.dto.RequestDto;
import ru.practicum.ewm.model.RequestModel;

import java.util.ArrayList;
import java.util.List;

public class RequestConverter {

    public static RequestModel convToModel(Long userId, Long eventId) {
        RequestModel model = new RequestModel();
        model.setEvent(eventId);
        model.setRequester(userId);
        return model;
    }

    public static RequestDto convToDto(RequestModel model) {
        RequestDto dto = new RequestDto();
        dto.setEvent(model.getEvent());
        dto.setRequester(model.getRequester());
        dto.setCreated(model.getCreated());
        dto.setStatus(model.getStatus());
        return dto;
    }

    public static List<RequestDto> mapToDto(List<RequestModel> requests) {
        List<RequestDto> res = new ArrayList<>();
        for (RequestModel r : requests) {
            res.add(convToDto(r));
        }
        return res;
    }
}
