package ru.practicum.ewm.util;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.exception.ParameterException;

public class PageHelper {
    public static PageRequest createRequest(int from, int size) {
        if (from < 0) {
            throw new ParameterException("Parameter from must be positive or zero.");
        }
        if (size <= 0) {
            throw new ParameterException("Parameter size must be positive.");
        }
        return PageRequest.of(from / size, size);
    }
}
