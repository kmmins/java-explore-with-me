package ru.practicum.ewm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.EventModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {

    private Long id;
    @NotBlank
    @Min(1)
    @Max(50)
    private String title;
    private Boolean pinned;
    private List<EventModel> events;

    public Boolean getPinned() {
        if (pinned == null) {
            pinned = false;
        }
        return pinned;
    }
}
