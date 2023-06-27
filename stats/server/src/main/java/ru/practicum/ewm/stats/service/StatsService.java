package ru.practicum.ewm.stats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.collective.*;
import ru.practicum.ewm.stats.exception.NotFoundException;
import ru.practicum.ewm.stats.exception.ParameterException;
import ru.practicum.ewm.stats.model.ConverterModelDto;
import ru.practicum.ewm.stats.model.HitModel;
import ru.practicum.ewm.stats.model.StatsModel;
import ru.practicum.ewm.stats.repository.StatsRepositoryJpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (start.isAfter(end)) {
            throw new ParameterException("Wrong date param");
        }
        if (uniq) {
            if (uris == null) {
                List<Object[]> rows = statsRepository.findAllUniqueIp(start, end);
                if (rows.size() == 0) {
                    throw new NotFoundException("No stats found for the specified parameters");
                }
                return getListModelFromRows(rows);
            }
            List<Object[]> rows = statsRepository.findStatsByUrisUniqueIp(start, end, uris);
            if (rows.size() == 0) {
                throw new NotFoundException("No stats found for the specified parameters");
            }
            return getListModelFromRows(rows);
        } else {
            if (uris == null) {
                List<Object[]> rows = statsRepository.findAll(start, end);
                if (rows.size() == 0) {
                    throw new NotFoundException("No stats found for the specified parameters");
                }
                return getListModelFromRows(rows);
            }
            List<Object[]> rows = statsRepository.findStatsByUris(start, end, uris);
            if (rows.size() == 0) {
                throw new NotFoundException("No stats found for the specified parameters");
            }
            return getListModelFromRows(rows);
        }
    }

    private List<StatsDto> getListModelFromRows(List<Object[]> rows) {
        List<StatsModel> result = new ArrayList<>();
        for (Object[] row : rows) {
            StatsModel statsModel = new StatsModel();
            statsModel.setApp(row[0].toString());
            statsModel.setUri(row[1].toString());
            statsModel.setHits(Long.valueOf(row[2].toString()));
            result.add(statsModel);
        }
        return ConverterModelDto.mapToDto(result);
    }
}
