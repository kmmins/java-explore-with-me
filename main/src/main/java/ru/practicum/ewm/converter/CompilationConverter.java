package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.dto.CompilationDto;
import ru.practicum.ewm.model.CompilationModel;

import java.util.ArrayList;
import java.util.List;

public class CompilationConverter {

    public static CompilationModel convToModel(CompilationDto dto) {
        return new CompilationModel(
                dto.getId(),
                dto.getTitle(),
                dto.getPinned(),
                dto.getEvents()
        );
    }

    public static CompilationDto convToDto(CompilationModel model) {
        return new CompilationDto(
                model.getId(),
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
