package ru.practicum.ewm.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.ewm.converter.CategoryConverter;
import ru.practicum.ewm.converter.EventConverter;
import ru.practicum.ewm.converter.RequestConverter;
import ru.practicum.ewm.exception.MainNotFoundException;
import ru.practicum.ewm.exception.MainParamConflictException;
import ru.practicum.ewm.exception.MainParameterException;
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
            throw new MainNotFoundException("User with id=" + userId + " was not found");
        }
        var dateTimeNow = LocalDateTime.now();
        Duration duration = Duration.between(dateTimeNow, eventDto.getEventDate());
        if (duration.toSeconds() <= 7200) {
            throw new MainParamConflictException("Event date must be not earlier than two hours later");
        }
        var createdEvent = EventConverter.convToModel(checkUser.get(), eventDto);
        var category = CategoryConverter.convToModel(categoryService.getCategoryById(eventDto.getCategory()));
        createdEvent.setCategory(category);
        createdEvent.setCreatedOn(LocalDateTime.now());
        createdEvent.setState(EventState.PENDING);
        var check = locationRepository.findByLatAndLon(eventDto.getLocation().getLat(), eventDto.getLocation().getLon());
        if (check.size() == 0) {
            LocationModel lc = new LocationModel();
            lc.setLat(eventDto.getLocation().getLat());
            lc.setLon(eventDto.getLocation().getLon());
            var after = locationRepository.save(lc);
            createdEvent.setLocation(after);
        } else {
            createdEvent.setLocation(check.get(0));
        }
        var afterCreate = eventRepository.save(createdEvent);
        return EventConverter.convToDtoFull(afterCreate);
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
        var result = eventRepository.findByIdAndAndInitiator(eventId, userId);
        if (result == null) {
            throw new MainNotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        return EventConverter.convToDto(result);
    }

    public EventDtoFull updateEventPrivate(Long userId, Long eventId, EventUpdateDto eventDto) {
        var eventToUpd = eventRepository.findByIdAndAndInitiator(eventId, userId);
        if (eventToUpd == null) {
            throw new MainNotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        if (eventToUpd.getState().equals(EventState.PUBLISHED)) {
            throw new MainParamConflictException("Updated event must be not published");
        }
        var dateTimeNow = LocalDateTime.now();
        if (eventDto.getEventDate() != null) {
            Duration duration = Duration.between(dateTimeNow, eventDto.getEventDate());
            if (eventDto.getEventDate().isBefore(dateTimeNow) || duration.toSeconds() <= 7200) {
                throw new MainParamConflictException("Event date must be not earlier than two hours later");
            }
        }
        if (eventDto.getAnnotation() != null) {
            eventToUpd.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            eventToUpd.setCategory(eventDto.getCategory());
        }
        if (eventDto.getDescription() != null) {
            eventToUpd.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            eventToUpd.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            eventToUpd.setLocation(eventDto.getLocation());
        }
        if (eventDto.getPaid() != null) {
            eventToUpd.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            eventToUpd.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            eventToUpd.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            eventToUpd.setTitle(eventDto.getTitle());
        }
        if (eventDto.getStateAction() != null) {
            if (eventToUpd.getState().equals(EventState.CANCELED) && eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                eventToUpd.setState(EventState.PENDING);
            }
            if (eventToUpd.getState().equals(EventState.PENDING) && eventDto.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                eventToUpd.setState(EventState.CANCELED);
            }
        }
        var after = eventRepository.save(eventToUpd);
        return EventConverter.convToDtoFull(after);
    }

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        var check = eventRepository.findByIdAndAndInitiator(eventId, userId);
        if (check == null) {
            throw new MainNotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        var listRequests = requestRepository.getEventRequests(eventId);
        return RequestConverter.mapToDto(listRequests);
    }

    public RequestUpdateResultDto updateStatusRequestsForEvent(Long userId, Long eventId, RequestUpdateDto requestDto) {
        var thisEvent = eventRepository.findByIdAndAndInitiator(eventId, userId);
        if (thisEvent == null) {
            throw new MainNotFoundException("Event with id=" + eventId + " and added by user id=" + userId + " was not found");
        }
        RequestUpdateResultDto afterUpdateStatus = new RequestUpdateResultDto();
        var allRequests = thisEvent.getAllRequests().stream()
                .collect(Collectors.toMap(RequestModel::getId, i -> i));
        var selectedRequests = requestDto.getRequestIds()
                .stream()
                .map(allRequests::get)
                .collect(Collectors.toList());
        if (selectedRequests.stream().anyMatch(Objects::isNull)) {
            throw new MainParameterException("Request not found for this event");
        }
        boolean check = selectedRequests.stream()
                .anyMatch(r -> !Objects.equals(r.getStatus(), RequestStatus.PENDING));
        if (check) {
            throw new MainParamConflictException("Request must have status PENDING");
        }
        if (thisEvent.getRequestModeration().equals(true) || thisEvent.getParticipantLimit() != 0) {
            long confReq = thisEvent.countConfirmedRequests();
            for (RequestModel r : selectedRequests) {
                if (thisEvent.getParticipantLimit() > confReq) {
                    if (requestDto.getStatus().equals(RequestUpdateStatus.APPROVED)) {
                        r.setStatus(RequestStatus.CONFIRMED);
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
                    if (requestDto.getStatus().equals(RequestUpdateStatus.APPROVED)) {
                        throw new MainParamConflictException("The participant limit has been reached");
                    }
                }
            }
        }
        return afterUpdateStatus;
    }

    public List<EventDtoFull> searchEventsAdmin(
            Long[] users,
            EventState states,
            Long[] categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        PageRequest pageRequest = PageHelper.createRequest(from, size);
        QEventModel qModelAdmin = QEventModel.eventModel;
        BooleanBuilder predicateAdmin = new BooleanBuilder();
        if (users != null) {
            predicateAdmin.and(qModelAdmin.initiator.id.in(users));
        }
        if (states != null) {
            predicateAdmin.and(qModelAdmin.state.in(states));
        }
        if (categories != null) {
            predicateAdmin.and(qModelAdmin.category.id.in(categories));
        }
        if (rangeStart != null) {
            predicateAdmin.and(qModelAdmin.eventDate.after(rangeStart));
        }
        if (rangeEnd != null) {
            predicateAdmin.and(qModelAdmin.eventDate.before(rangeEnd));
        }
        List<EventModel> foundEventsAdmin = new ArrayList<>();
        eventRepository.findAll(predicateAdmin).forEach(foundEventsAdmin::add);
        if (foundEventsAdmin.size() == 0) {
            return new ArrayList<>();
        }
        List<EventModel> pageList = foundEventsAdmin.stream()
                .skip(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .collect(Collectors.toList());
        var result = EventConverter.mapToDtoFull(pageList);
        setViewsForListFullDto(result);
        return result;
    }

    public EventDtoFull updateEventByAdmin(Long eventId, EventUpdateDto eventDto) {
        var check = eventRepository.findById(eventId);
        if (check.isEmpty()) {
            throw new MainNotFoundException("Event with id=" + eventId + " was not found");
        }
        var eventToUpdAdmin = check.get();
        var dateTimeNow = LocalDateTime.now();
        if (eventDto.getEventDate() != null) {
            Duration duration = Duration.between(dateTimeNow, eventDto.getEventDate());
            if (eventDto.getEventDate().isBefore(dateTimeNow) || duration.toSeconds() <= 7200) {
                throw new MainParamConflictException("Event date must be not earlier than two hours later");
            }
        }
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
            eventDto.setEventDate(eventDto.getEventDate());
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
        if (eventDto.getTitle() != null) {
            eventToUpdAdmin.setTitle(eventDto.getTitle());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                if (!eventToUpdAdmin.getState().equals(EventState.PENDING)) {
                    throw new MainParamConflictException("Cannot publish event because it's not in the pending state");
                }
                if (eventDto.getEventDate() != null) {
                    var datePublish = LocalDateTime.now();
                    Duration duration = Duration.between(datePublish, eventDto.getEventDate());
                    if (eventDto.getEventDate().isBefore(datePublish) || duration.toSeconds() <= 3600) {
                        throw new MainParamConflictException("Event date must be not earlier than one hour before published");
                    }
                    eventToUpdAdmin.setState(EventState.PUBLISHED);
                    eventToUpdAdmin.setPublishedOn(datePublish);
                }
            }
            if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                if (eventToUpdAdmin.getState().equals(EventState.PUBLISHED)) {
                    throw new MainParamConflictException("Cannot reject event because it's in the published state");
                }
                eventToUpdAdmin.setState(EventState.CANCELED);
            }
        }
        var after = eventRepository.save(eventToUpdAdmin);
        return EventConverter.convToDtoFull(after);
    }

    public List<EventDtoFull> getEventsPublic(
            String text,
            Long[] categories,
            Boolean paid,
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
        if (rangeStart == null) {
            rangeStart = dateTimeNow;
        }
        QEventModel qModel = QEventModel.eventModel;
        BooleanExpression predicatePublic = qModel.eventDate.after(rangeStart).and(qModel.state.eq(EventState.PUBLISHED));
        if (rangeEnd != null && rangeEnd.isAfter(dateTimeNow)) {
            predicatePublic = predicatePublic.and(qModel.eventDate.before(rangeEnd));
        }
        if (text != null) {
            predicatePublic = predicatePublic.and(qModel.annotation.containsIgnoreCase(text).or(qModel.description.containsIgnoreCase(text)));
        }
        if (categories != null) {
            predicatePublic = predicatePublic.and(qModel.category.id.in(categories));
        }
        if (paid != null) {
            predicatePublic = predicatePublic.and(qModel.paid.eq(paid));
        }
        List<EventModel> foundEvents = new ArrayList<>();
        eventRepository.findAll(predicatePublic).forEach(foundEvents::add);
        if (onlyAvailable) {
            foundEvents = foundEvents.stream()
                    .filter(e -> e.countConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (foundEvents.size() == 0) {
            return new ArrayList<>();
        } else {
            List<EventModel> eventsPageAndSort = new ArrayList<>();
            if (sort == null) {
                eventsPageAndSort = foundEvents.stream()
                        .skip(pageRequest.getOffset())
                        .limit(pageRequest.getPageSize())
                        .collect(Collectors.toList());
            }
            if (sort != null && sort.equals(EventSort.EVENT_DATE)) {
                eventsPageAndSort = foundEvents.stream()
                        .sorted(Comparator.comparing(EventModel::getEventDate))
                        .skip(pageRequest.getOffset())
                        .limit(pageRequest.getPageSize())
                        .collect(Collectors.toList());
            }
            if (sort != null && sort.equals(EventSort.VIEWS)) {
                eventsPageAndSort = foundEvents.stream()
                        .sorted(Comparator.comparing(this::getViews).reversed())
                        .skip(pageRequest.getOffset())
                        .limit(pageRequest.getPageSize())
                        .collect(Collectors.toList());
            }
            statsClient.saveStats("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), dateTimeNow);//?
            var result = EventConverter.mapToDtoFull(eventsPageAndSort);
            setViewsForListFullDto(result);
            return result;
        }
    }

    public EventDtoFull getEventByIdPublic(Long id, HttpServletRequest request) {
        EventModel foundEvent = eventRepository.findByIdPublished(id, EventState.PUBLISHED.toString());
        if (foundEvent == null) {
            throw new MainNotFoundException("Event with id=" + id + " was not found");
        }
        var dateTimeNow = LocalDateTime.now();
        statsClient.saveStats("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), dateTimeNow);
        Long viewsFromStats = getViews(foundEvent);
        var result = EventConverter.convToDtoFull(foundEvent);
        result.setViews(viewsFromStats);
        return result;
    }

    private Long getViews(EventModel event) {
        long id = event.getId();
        String[] uris = {"/events/{" + id + "}"};
        List<StatsDto> stats = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), uris, true);
        return stats.get(0).getHits();
    }

    private void setViewsForListFullDto(List<EventDtoFull> events) {
        if (events.size() != 0) {
            String[] uris = new String[events.size()];
            for (int i = 0; i < uris.length; i++) {
                long id = events.get(i).getId();
                uris[i] = "/events/{" + id + "}";
            }
            var min = events.stream()
                    .min(Comparator.comparing(EventDtoFull::getCreatedOn))
                    .orElse(events.get(0));
            LocalDateTime start = min.getCreatedOn();
            LocalDateTime dateTime = LocalDateTime.now();
            List<StatsDto> stats = Arrays.asList(new StatsDto[events.size()]);
            try {
                stats = statsClient.getStats(start, dateTime, uris, true);
            } catch (HttpClientErrorException e) {
                System.out.println(e.getMessage());
            }
            for (int i = 0; i < uris.length; i++) {
                if (stats.get(i) != null) {
                    events.get(i).setViews(stats.get(i).getHits());
                } else {
                    events.get(i).setViews(0L);
                }
            }
        }
    }
}

