package com.ricky.collaboration.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorMessage {

    private String type;
    private String sessionId;
    private String oderId;
    private String username;
    private Integer position;
    private Integer selectionStart;
    private Integer selectionEnd;

    public static CursorMessage of(String sessionId, String oderId, String username, int position) {
        return CursorMessage.builder()
                .type("cursor")
                .sessionId(sessionId)
                .oderId(oderId)
                .username(username)
                .position(position)
                .selectionStart(position)
                .selectionEnd(position)
                .build();
    }

    public static CursorMessage of(String sessionId, String oderId, String username,
                                   int position, int selectionStart, int selectionEnd) {
        return CursorMessage.builder()
                .type("cursor")
                .sessionId(sessionId)
                .oderId(oderId)
                .username(username)
                .position(position)
                .selectionStart(selectionStart)
                .selectionEnd(selectionEnd)
                .build();
    }
}
