package ru.practicum.ewm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.converter.CommentConverter;
import ru.practicum.ewm.exception.MainNotFoundException;
import ru.practicum.ewm.exception.MainParameterException;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.dto.CommentDto;
import ru.practicum.ewm.repository.*;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(EventRepository eventRepository,
                          UserRepository userRepository,
                          RequestRepository requestRepository,
                          CommentRepository commentRepository) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public CommentDto addComment(Long userId, Long eventId, CommentDto commentDto) {
        var checkUser = userRepository.findById(userId);
        if (checkUser.isEmpty()) {
            throw new MainNotFoundException("User with id=" + userId + " was not found");
        }
        var user = checkUser.get();
        var checkEvent = eventRepository.findById(eventId);
        if (checkEvent.isEmpty()) {
            throw new MainNotFoundException("Event with id=" + eventId + " was not found");
        }
        var event = checkEvent.get();
        var dateTime = LocalDateTime.now();
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new MainParameterException("Only published event can be commented");
        }
        if (dateTime.isAfter(event.getEventDate()) && (event.getRequestModeration() || event.getParticipantLimit() != 0)) {
            var checkRequester = requestRepository.getRequestForAddComment(eventId, userId, RequestStatus.CONFIRMED.toString());
            if (checkRequester == null || !userId.equals(event.getInitiator().getId())) {
                throw new MainParameterException("Only confirmed requester or initiator can leave comments when event get started");
            }
        }
        var created = CommentConverter.convertToModel(user, event, commentDto);
        var after = commentRepository.save(created);
        return CommentConverter.convertToDto(after);
    }

    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentDto updCommentDto) {
        var checkUser = userRepository.findById(userId);
        if (checkUser.isEmpty()) {
            throw new MainNotFoundException("User with id=" + userId + " was not found");
        }
        var user = checkUser.get();
        var checkEvent = eventRepository.findById(eventId);
        if (checkEvent.isEmpty()) {
            throw new MainNotFoundException("Event with id=" + eventId + " was not found");
        }
        var event = checkEvent.get();
        var checkComment = commentRepository.findById(commentId);
        if (checkComment.isEmpty()) {
            throw new MainNotFoundException("Comment with id=" + commentId + " was not found");
        }
        var comment = checkComment.get();
        if (userId.equals(comment.getAuthor().getId())) {
            var after = commentRepository.save(CommentConverter.convertToModel(user, event, updCommentDto));
            return CommentConverter.convertToDto(after);
        } else {
            throw new MainParameterException("Only author can update comment");
        }
    }

    public void deleteComment(Long userId, Long eventId, Long commentId) {
        var checkUser = userRepository.findById(userId);
        if (checkUser.isEmpty()) {
            throw new MainNotFoundException("User with id=" + userId + " was not found");
        }
        var checkEvent = eventRepository.findById(eventId);
        if (checkEvent.isEmpty()) {
            throw new MainNotFoundException("Event with id=" + eventId + " was not found");
        }
        var event = checkEvent.get();
        var checkComment = commentRepository.findById(commentId);
        if (checkComment.isEmpty()) {
            throw new MainNotFoundException("Comment with id=" + commentId + " was not found");
        }
        var comment = checkComment.get();
        if (userId.equals(event.getInitiator().getId()) || userId.equals(comment.getAuthor().getId())) {
            commentRepository.deleteById(commentId);
        } else {
            throw new MainParameterException("Only author or event initiator can delete comment");
        }
    }
}
