package ru.practicum.ewm.service;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.converter.CategoryConverter;
import ru.practicum.ewm.converter.EventConverter;
import ru.practicum.ewm.converter.RequestConverter;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ParamConflictException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.model.dto.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.LocationRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.collective.StatsDto;
import ru.practicum.ewm.util.PageHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final CategoryService categoryService;

    @Autowired
    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        RequestRepository requestRepository,
                        LocationRepository locationRepository,
                        StatsClient statsClient,
                        CategoryService categoryService) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.statsClient = statsClient;
        this.categoryService = categoryService;
    }

    public EventDtoFull addEvent(Long userId, EventDto eventDto) {
        var checkUser = userRepository.findById(userId);
        if (checkUser.isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        var dateTimeNow = LocalDateTime.now();
        Duration duration = Duration.between(dateTimeNow, eventDto.getEventDate());
        if (duration.toSeconds() <= 7200) {
            throw new ParamConflictException("Event date must be not earlier than two hours later");
        }
        var created = EventConverter.convToModel(checkUser.get(), eventDto);
        Long catId = eventDto.getCategory();
        CategoryModel cat = CategoryConverter.convToModel(categoryService.getCategoryById(catId));
        created.setCategory(cat);
        created.setState(EventState.PENDING);

        var check = locationRepository.findByLatAndLon(eventDto.getLocation().getLat(), eventDto.getLocation().getLon());
        if (check.size() == 0) {
            LocationModel lc = new LocationModel();
            lc.setLat(eventDto.getLocation().getLat());
            lc.setLon(eventDto.getLocation().getLon());
            var after = locationRepository.save(lc);
            created.setLocation(after);
        } else {
            created.setLocation(check.get(0));
        }
        return EventConverter.convToDtoFull(eventRepository.save(created));
    }

    public List<EventDto> getAllEventsByInitiatorPrivate(Long userId, int from, int size) {
        PageRequest pageRequest = PageHelper.createRequest(from, size);
        var result = eventRepository.findAllByInitiator(userId, pageRequest).getContent();
        if (result.size() == 0) {
            return new ArrayList<>();
        }
        return EventConverter.mapToDto(result);
    }

    public EventDto getEventByIdPrivate(Long userId, Long eventId) {
        var result = eventRepository.findByIdAndAndInitiator(userId, eventId);
        if (result == null) {
            throw new NotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        return EventConverter.convToDto(result);
    }

    public EventDto updateEvent(Long userId, Long eventId, EventUpdateDto eventDto) {
        var eventToUpd = eventRepository.findByIdAndAndInitiator(userId, eventId);
        if (eventToUpd == null) {
            throw new NotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        var dateTimeNow = LocalDateTime.now();
        Duration duration = Duration.between(dateTimeNow, eventDto.getEventDate());
        if (duration.toSeconds() <= 7200) {
            throw new ParamConflictException("Event date must be not earlier than two hours later");
        }
        if (eventToUpd.getState().equals(EventState.PUBLISHED)) {
            throw new ParamConflictException("Updated event must be not published");
        }
        eventUpdateFields(eventDto, eventToUpd);
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction() == EventStateAction.SEND_TO_REVIEW) {
                eventToUpd.setState(EventState.PENDING);
            } else {
                eventToUpd.setState(EventState.CANCELED);
            }
        }
        if (eventDto.getTitle() != null) {
            eventToUpd.setTitle(eventDto.getTitle());
        }
        var after = eventRepository.save(eventToUpd);
        return EventConverter.convToDto(after);
    }

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        var check = eventRepository.findByIdAndAndInitiator(userId, eventId);
        if (check == null) {
            throw new NotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        var listRequests = requestRepository.getEventRequests(eventId);
        return RequestConverter.mapToDto(listRequests);
    }

    //???
    public RequestUpdateResultDto updateStatusRequestsForEvent(Long userId, Long eventId, RequestUpdateDto requestDto) {
        var thisEvent = eventRepository.findByIdAndAndInitiator(userId, eventId);
        if (thisEvent == null) {
            throw new NotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }

        RequestUpdateResultDto afterUpdateStatus = new RequestUpdateResultDto();

        var allRequests = thisEvent.getAllRequests().stream().collect(Collectors.toMap(RequestModel::getId, i -> i));
        var selectedRequests = requestDto.getRequestIds()
                .stream()
                .map(allRequests::get)
                .collect(Collectors.toList());
        boolean check = selectedRequests.stream()
                .anyMatch(r -> !Objects.equals(r.getStatus(), RequestStatus.PENDING));
        if (check) {
            throw new ParamConflictException("Request must have status PENDING");
        }

        if (thisEvent.getRequestModeration().equals(true) || thisEvent.getParticipantLimit() != 0) {
            long confReq = thisEvent.countConfirmedRequests();
            for (RequestModel r : selectedRequests) {
                if (thisEvent.getParticipantLimit() > confReq) {
                    if (requestDto.getStatus().equals(RequestUpdateStatus.CONFIRMED)) {
                        r.setStatus(RequestStatus.APPROVED);
                        confReq++;
                        if (thisEvent.getParticipantLimit() == confReq) {
                            for (RequestModel rm : thisEvent.getAllRequests()) {
                                if (rm.getStatus().equals(RequestStatus.PENDING)) {
                                    rm.setStatus(RequestStatus.REJECTED);
                                    requestRepository.save(rm);
                                }
                            }
                        }
                        var rSaved = requestRepository.save(r);
                        afterUpdateStatus.getConfirmedRequests().add(RequestConverter.convToDto(rSaved));
                    }
                    if (requestDto.getStatus().equals(RequestUpdateStatus.REJECTED)) {
                        r.setStatus(RequestStatus.REJECTED);
                        var rSaved = requestRepository.save(r);
                        afterUpdateStatus.getRejectedRequests().add(RequestConverter.convToDto(rSaved));
                    }
                } else {
                    if (requestDto.getStatus().equals(RequestUpdateStatus.CONFIRMED)) {
                        throw new ParamConflictException("The participant limit has been reached");
                    }
                }
            }
        }

        return afterUpdateStatus;
    }

    public List<EventDtoFull> searchEventsAdmin(
            Long[] users,
            EventState[] states,
            Long[] categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        PageRequest pageRequest = PageHelper.createRequest(from, size);
        QEventModel qModelAdmin = QEventModel.eventModel;
        Predicate predicate = qModelAdmin.state.in(states)
                .and(qModelAdmin.initiator.id.in(users))
                .and(qModelAdmin.category.id.in(categories))
                .and(qModelAdmin.eventDate.between(rangeStart, rangeEnd));

        var allFoundEventsAdmin = eventRepository.findAll(predicate, pageRequest);
        if (allFoundEventsAdmin.getContent().size() == 0) {
            return new ArrayList<>();
        }
        return EventConverter.mapToDtoFull(allFoundEventsAdmin.getContent());
    }

    public EventDtoFull updateEventByAdmin(Long eventId, EventUpdateDto eventDto) {
        var check = eventRepository.findById(eventId);
        if (check.isEmpty()) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        var eventToUpdAdmin = check.get();
        Duration duration = Duration.between(eventToUpdAdmin.getPublishedOn(), eventToUpdAdmin.getEventDate());
        if (duration.toSeconds() <= 3600) {
            throw new ParamConflictException("Event date must be not earlier than one hour before published");
        }
        eventUpdateFields(eventDto, eventToUpdAdmin);
        if (eventDto.getStateAction() != null) {
            if (eventToUpdAdmin.getState().equals(EventState.PUBLISHED)) {
                throw new ParamConflictException("Cannot update the event because it's not in the right state");
            }
            if (eventDto.getStateAction() == EventStateAction.SEND_TO_REVIEW) {
                eventToUpdAdmin.setState(EventState.PUBLISHED);
                eventToUpdAdmin.setPublishedOn(LocalDateTime.now());
            }
            if (eventDto.getStateAction() == EventStateAction.REJECT_EVENT) {
                eventToUpdAdmin.setState(EventState.CANCELED);
            }
        }
        if (eventDto.getTitle() != null) {
            eventToUpdAdmin.setTitle(eventDto.getTitle());
        }
        var after = eventRepository.save(eventToUpdAdmin);
        return EventConverter.convToDtoFull(after);
    }

    //??
    public List<EventDtoFull> getEventsPublic(
            String text,
            Long[] categories,
            boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            int from,
            int size,
            HttpServletRequest request
    ) {
        PageRequest pageRequest = PageHelper.createRequest(from, size);
        var dateTimeNow = LocalDateTime.now();
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = dateTimeNow;
            rangeEnd = LocalDateTime.of(3000, 1, 1, 1, 1, 0); //?
        }
        QEventModel qModel = QEventModel.eventModel;
        Predicate predicateAll = qModel.annotation.containsIgnoreCase(text).or(qModel.description.containsIgnoreCase(text))
                .and(qModel.state.eq(EventState.PUBLISHED))
                .and(qModel.eventDate.between(rangeStart, rangeEnd))
                .and(qModel.category.id.in(categories))
                .and(qModel.paid.eq(paid));
        List<EventModel> foundEvents = new ArrayList<>();
        eventRepository.findAll(predicateAll).forEach(foundEvents::add);
        if (onlyAvailable) {
            foundEvents = foundEvents.stream()
                    .filter(e -> e.countConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (foundEvents.size() == 0) {
            return new ArrayList<>();
        } else {
            List<EventModel> eventsSorted = new ArrayList<>();
            if (sort.equals(EventSort.EVENT_DATE)) {
                eventsSorted = foundEvents.stream()
                        .sorted(Comparator.comparing(EventModel::getEventDate))
                        .skip(pageRequest.getOffset())
                        .limit(pageRequest.getPageSize())
                        .collect(Collectors.toList());
            }
            if (sort.equals(EventSort.VIEWS)) {
                eventsSorted = foundEvents.stream()
                        .sorted(Comparator.comparing(this::getViews).reversed())
                        .skip(pageRequest.getOffset())
                        .limit(pageRequest.getPageSize())
                        .collect(Collectors.toList());
            }
            statsClient.saveStats("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), dateTimeNow);
            return EventConverter.mapToDtoFull(eventsSorted);
        }
    }

    //??
    public EventDtoFull getEventByIdPublic(Long id, HttpServletRequest request) {
        EventModel foundEvent = eventRepository.findByIdPublished(id, EventState.PUBLISHED.toString()); //приджоинить конф реквест для этого квента
        if (foundEvent == null) {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }
        var dateTimeNow = LocalDateTime.now();
        statsClient.saveStats("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), dateTimeNow);
        Long viewsFromStats = getViews(foundEvent);
        var result = EventConverter.convToDtoFull(foundEvent);
        result.setViews(viewsFromStats);
        return result;
    }

    private void eventUpdateFields(EventUpdateDto eventDto, EventModel eventToUpdAdmin) {
        if (eventDto.getAnnotation() != null) {
            eventToUpdAdmin.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            eventToUpdAdmin.setCategory(eventDto.getCategory());
        }
        if (eventDto.getDescription() != null) {
            eventToUpdAdmin.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            eventToUpdAdmin.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            eventToUpdAdmin.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            eventToUpdAdmin.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            eventToUpdAdmin.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            eventToUpdAdmin.setRequestModeration(eventDto.getRequestModeration());
        }
    }

    private Long getViews(EventModel event) {
        long id = event.getId();
        String[] uris = {"/events/{" + id + "}"};
        List<StatsDto> stats = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), uris, true);
        return stats.get(0).getHits();
    }
}
