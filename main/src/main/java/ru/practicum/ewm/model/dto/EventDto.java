package ru.practicum.ewm.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.LocationModel;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private Long category;
    @NotNull
    private LocationModel location;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Integer participantLimit;
    private Boolean requestModeration;
    private Boolean paid;

    public Integer getParticipantLimit() {
        if (participantLimit == null) {
            participantLimit = 0;
        }
        return participantLimit;
    }

    public Boolean getRequestModeration() {
        if (requestModeration == null) {
            requestModeration = true;
        }
        return requestModeration;
    }

    public Boolean getPaid() {
        if (paid == null) {
            paid = false;
        }
        return paid;
    }
}
