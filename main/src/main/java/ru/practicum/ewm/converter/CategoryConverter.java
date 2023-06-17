package ru.practicum.ewm.converter;

import ru.practicum.ewm.model.dto.CategoryDto;
import ru.practicum.ewm.model.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryConverter {

    public static CategoryModel convToModel(CategoryDto dto) {
        return new CategoryModel(
                dto.getId(),
                dto.getName()
        );
    }

    public static CategoryDto convToDto(CategoryModel model) {
        return new CategoryDto(
                model.getId(),
                model.getName()
        );
    }

    public static List<CategoryDto> mapToDto(List<CategoryModel> cats) {
        List<CategoryDto> res = new ArrayList<>();
        for (CategoryModel c : cats) {
            res.add(convToDto(c));
        }
        return res;
    }
}
