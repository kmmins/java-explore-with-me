package ru.practicum.ewm.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "events", schema = "public")
public class EventModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "annotation", nullable = false)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryModel category;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private UserModel initiator;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationModel location;
    @Column(name = "eventDate")
    private LocalDateTime eventDate;
    @Column(name = "createdOn")
    private LocalDateTime createdOn;
    @Column(name = "publishedOn")
    private LocalDateTime publishedOn;
    @Column(name = "participantLimit")
    private Integer participantLimit;
    @OneToMany
    @JoinColumn(name = "event_id")
    private List<RequestModel> allRequests;
    @Column(name = "requestModeration")
    private Boolean requestModeration;
    @Column(name = "paid")
    private Boolean paid;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state;

    public long countConfirmedRequests() {
        long result = 0L;
        for (RequestModel rm : allRequests) {
            if (rm.getStatus().equals(RequestStatus.APPROVED)) {
                result++;
            }
        }
        return result;
    }
}
