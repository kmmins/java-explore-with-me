package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.ewm.model.CommentModel;

@EnableJpaRepositories
public interface CommentRepository extends JpaRepository<CommentModel, Long> {

    @Query(value = "select * from comments where event_id in (select events.id from events where state=?1 and events.id=?2) and comments.id=?3", nativeQuery = true)
    CommentModel getByIdForEvent(String state, Long eventId, Long commentId);

    @Query(value = "select * from comments where event_id in (select events.id from events where state=?1 and events.id=?2)", nativeQuery = true)
    Page<CommentModel> getAllForEvent(String state, Long eventId, PageRequest pageRequest);
}
