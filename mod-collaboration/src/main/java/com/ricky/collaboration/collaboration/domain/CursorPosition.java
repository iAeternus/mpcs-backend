package com.ricky.collaboration.collaboration.domain;

import lombok.*;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(force = true)
public class CursorPosition {

    private String userId;
    private String username;
    private int position;
    private int selectionStart;
    private int selectionEnd;
    private Instant updatedAt;

    public static CursorPosition of(String userId, String username, int position) {
        return CursorPosition.builder()
                .userId(userId)
                .username(username)
                .position(position)
                .selectionStart(position)
                .selectionEnd(position)
                .updatedAt(Instant.now())
                .build();
    }

    public static CursorPosition of(String userId, String username, int position, int selectionStart, int selectionEnd) {
        return CursorPosition.builder()
                .userId(userId)
                .username(username)
                .position(position)
                .selectionStart(selectionStart)
                .selectionEnd(selectionEnd)
                .updatedAt(Instant.now())
                .build();
    }

    public CursorPosition withPosition(int newPosition) {
        return CursorPosition.of(userId, username, newPosition, newPosition, newPosition);
    }

    public CursorPosition withSelection(int newPosition, int newSelectionStart, int newSelectionEnd) {
        return CursorPosition.of(userId, username, newPosition, newSelectionStart, newSelectionEnd);
    }

    public CursorPosition withTimestamp() {
        return CursorPosition.builder()
                .userId(userId)
                .username(username)
                .position(position)
                .selectionStart(selectionStart)
                .selectionEnd(selectionEnd)
                .updatedAt(Instant.now())
                .build();
    }
}
