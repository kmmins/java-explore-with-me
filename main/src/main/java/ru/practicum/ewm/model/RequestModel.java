package ru.practicum.ewm.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "requests", schema = "public")
public class RequestModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventModel event;
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private UserModel requester;
    @Column(name = "created")
    private LocalDateTime created;
    @Column(name = "status")
    private RequestStatus status;
}
