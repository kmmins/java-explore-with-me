package ru.practicum.ewm.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateDto {

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    private CategoryModel category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationModel location;
    private Integer participantLimit;
    private Boolean requestModeration;
    private Boolean paid;
    private EventStateAction stateAction;
}
