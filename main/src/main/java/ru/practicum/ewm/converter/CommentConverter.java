package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.CommentModel;
import ru.practicum.ewm.model.EventModel;
import ru.practicum.ewm.model.UserModel;
import ru.practicum.ewm.model.dto.CommentDto;

import java.util.ArrayList;
import java.util.List;

public class CommentConverter {
    public static CommentDto convertToDto(CommentModel model) {
        return new CommentDto(
                model.getId(),
                model.getText(),
                model.getAuthor().getName(),
                model.getCreated()
        );
    }

    public static CommentModel convertToModel(UserModel user, EventModel event, CommentDto dto) {
        CommentModel model = new CommentModel();
        model.setText(dto.getText());
        model.setAuthor(user);
        model.setEvent(event);
        model.setCreated(dto.getCreated());
        return model;
    }

    public static List<CommentDto> mapToDto(List<CommentModel> comments) {
        if (comments == null) {
            return null;
        }
        List<CommentDto> result = new ArrayList<>();
        for (CommentModel c : comments) {
            result.add(convertToDto(c));
        }
        return result;
    }
}
