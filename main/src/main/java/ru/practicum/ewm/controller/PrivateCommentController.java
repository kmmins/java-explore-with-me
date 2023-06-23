package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.dto.*;
import ru.practicum.ewm.service.CommentService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateCommentController {

    private final CommentService commentService;

    @Autowired
    public PrivateCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{eventId}/comment")
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody CommentDto commentDto) {
        var addedCommentDto = commentService.addComment(userId, eventId, commentDto);
        log.info("[POST /users/{userId}/events/{eventId}/comment] (Private). " +
                "Add comment (dto): {}, from user (id): {}, for event (id): {}", commentDto, eventId, userId);
        return addedCommentDto;
    }

    @PatchMapping("/{eventId}/comment/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody CommentDto updCommentDto) {
        var res = commentService.updateComment(userId, eventId, commentId, updCommentDto);
        log.info("[PATCH /users/{userId}/events/{eventId}/comment/{commentId}] (Private). " +
                "Patch comment (id): {} for event (id): {}, to comment (dto): {}, by user (id): {}", userId, eventId, commentId, updCommentDto);
        return res;
    }

    @DeleteMapping("/{eventId}/comment/{commentId}")
    public void deletedComment(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @PathVariable Long commentId) {
        commentService.deleteComment(userId, eventId, commentId);
        log.info("[DELETE /users/{userId}/events/{eventId}/comment/{commentId}] (Private). " +
                "Delete comment (id): {} for event (id): {}, by user (id): {}", userId, eventId, commentId);
    }
}
