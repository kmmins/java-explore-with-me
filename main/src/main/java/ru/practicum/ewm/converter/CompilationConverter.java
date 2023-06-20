package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.dto.CompilationDto;
import ru.practicum.ewm.model.CompilationModel;

import java.util.ArrayList;
import java.util.List;

public class CompilationConverter {

    public static CompilationModel convToModel(CompilationDto dto) {
        CompilationModel model = new CompilationModel();
        model.setTitle(dto.getTitle());
        model.setPinned(dto.getPinned());
        model.setEvents(dto.getEvents());
        return model;
    }

    public static CompilationDto convToDto(CompilationModel model) {
        return new CompilationDto(
                model.getTitle(),
                model.getPinned(),
                model.getEvents()
        );
    }

    public static List<CompilationDto> mapToDto(List<CompilationModel> comps) {
        List<CompilationDto> res = new ArrayList<>();
        for (CompilationModel c : comps) {
            res.add(convToDto(c));
        }
        return res;
    }
}
