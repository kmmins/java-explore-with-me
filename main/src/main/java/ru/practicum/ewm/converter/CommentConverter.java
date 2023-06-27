package ru.practicum.ewm.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.CommentModel;
import ru.practicum.ewm.model.EventModel;
import ru.practicum.ewm.model.UserModel;
import ru.practicum.ewm.model.dto.CommentDto;
import ru.practicum.ewm.model.dto.CommentShortDto;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static CommentShortDto convertToShortDto(CommentModel model) {
        return new CommentShortDto(
                model.getText(),
                model.getAuthor().getName(),
                model.getCreated()
        );
    }

    public static List<CommentShortDto> mapToShortDto(List<CommentModel> comments) {
        if (comments == null) {
            return null;
        }
        List<CommentShortDto> result = new ArrayList<>();
        for (CommentModel c : comments) {
            result.add(convertToShortDto(c));
        }
        return result;
    }
}
