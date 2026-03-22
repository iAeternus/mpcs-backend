package com.ricky.collaboration.collaboration.domain.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(force = true)
public class TextOperation {
    
    private String id;
    private TextOperationType type;
    private int position;
    private String content;
    private int length;
    private String userId;
    private long clientVersion;
    private Instant timestamp;
    
    public static TextOperation insert(String userId, int position, String content, long clientVersion) {
        return TextOperation.builder()
                .id(UUID.randomUUID().toString())
                .type(TextOperationType.INSERT)
                .position(position)
                .content(content)
                .length(content != null ? content.length() : 0)
                .userId(userId)
                .clientVersion(clientVersion)
                .timestamp(Instant.now())
                .build();
    }
    
    public static TextOperation delete(String userId, int position, int length, long clientVersion) {
        return TextOperation.builder()
                .id(UUID.randomUUID().toString())
                .type(TextOperationType.DELETE)
                .position(position)
                .content(null)
                .length(length)
                .userId(userId)
                .clientVersion(clientVersion)
                .timestamp(Instant.now())
                .build();
    }
    
    public static TextOperation retain(String userId, int position, int length, long clientVersion) {
        return TextOperation.builder()
                .id(UUID.randomUUID().toString())
                .type(TextOperationType.RETAIN)
                .position(position)
                .content(null)
                .length(length)
                .userId(userId)
                .clientVersion(clientVersion)
                .timestamp(Instant.now())
                .build();
    }
    
    public boolean isInsert() {
        return type == TextOperationType.INSERT;
    }
    
    public boolean isDelete() {
        return type == TextOperationType.DELETE;
    }
    
    public boolean isRetain() {
        return type == TextOperationType.RETAIN;
    }
}
