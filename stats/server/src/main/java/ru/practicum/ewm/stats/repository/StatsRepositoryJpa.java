package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.ewm.stats.model.HitModel;
import ru.practicum.ewm.stats.model.StatsModel;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories
public interface StatsRepositoryJpa extends JpaRepository<HitModel, Long> {

    @Query(value = "select application, uri, count(remote_ip) as countIp " +
            "from hits where uri in :uris " +
            "and (date_time between :start and :end) " +
            "group by application, uri", nativeQuery = true)
    List<StatsModel> findStatsByUris(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query(value = "select application, uri, count(distinct remote_ip) as countIp " +
            "from hits where uri in :uris " +
            "and (date_time >=:start and date_time =<:end) " +
            "group by application, uri", nativeQuery = true)
    List<StatsModel> findStatsByUrisUniqueIp(LocalDateTime start, LocalDateTime end, String[] uris);
}
