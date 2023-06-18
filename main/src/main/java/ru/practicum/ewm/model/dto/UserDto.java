package ru.practicum.ewm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    @Email
    @NotBlank
    @Min(6)
    @Max(254)
    private String email;
    @NotBlank
    @Min(2)
    @Max(200)
    private String name;
}
