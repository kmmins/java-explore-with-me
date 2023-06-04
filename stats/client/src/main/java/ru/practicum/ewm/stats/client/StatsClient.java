package ru.practicum.ewm.stats.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.model.HitDto;
import ru.practicum.ewm.stats.model.StatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsClient {

    private final RestTemplate rest;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate rest) {
        this.rest = rest;
    }


    public ResponseEntity<Void> saveStats(String app, String uri, String ip) {
        HitDto body = new HitDto(app, uri, ip, LocalDateTime.now());
        return rest.postForEntity("/hit", body, Void.class);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean uniq) {
        Map<String, Object> parameters = Map.of(
                "start", encodeDateTime(start),
                "end", encodeDateTime(end),
                "uris", uris,
                "unique", uniq
        );
        ResponseEntity<StatsDto[]> response = rest.getForEntity(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                StatsDto[].class,
                parameters);
        StatsDto[] result = response.getBody();
        return Arrays.stream(result)
                .collect(Collectors.toList());
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        String dateTimeString = dateTime.format(formatter);
        return URLEncoder.encode(dateTimeString, StandardCharsets.UTF_8);
    }
}
