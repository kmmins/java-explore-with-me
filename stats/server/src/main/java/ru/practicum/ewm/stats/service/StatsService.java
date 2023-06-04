package ru.practicum.ewm.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.model.*;
import ru.practicum.ewm.stats.repository.StatsRepositoryJpa;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepositoryJpa statsRepository;

    @Autowired
    public StatsService(StatsRepositoryJpa statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void addHit(HitDto hitDto) {
        HitModel addedHitModel = ConverterModelDto.convertToModel(hitDto);
        statsRepository.save(addedHitModel);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, boolean uniq) {
        if (uniq) {
            List<StatsModel> statsUniq = statsRepository.findStatsByUrisUniqueIp(start, end, uris);
            return ConverterModelDto.mapToDto(statsUniq);
        }
        List<StatsModel> stats = statsRepository.findStatsByUris(start, end, uris);
        return ConverterModelDto.mapToDto(stats);
    }
}
