package com.ricky.collaboration.collaboration.domain.ot;

import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;
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
    
    public static TextOperation buildOperation(SubmitOperationCommand command, UserContext userContext) {
        TextOperationType type = command.getType();
        
        return switch (type) {
            case INSERT -> {
                if (command.getContent() == null || command.getContent().isEmpty()) {
                    throw MyException.requestValidationException("INSERT操作必须提供content");
                }
                yield TextOperation.insert(
                        userContext.getUid(),
                        command.getPosition(),
                        command.getContent(),
                        command.getClientVersion()
                );
            }
            case DELETE -> {
                if (command.getLength() == null || command.getLength() <= 0) {
                    throw MyException.requestValidationException("DELETE操作必须提供有效的length");
                }
                yield TextOperation.delete(
                        userContext.getUid(),
                        command.getPosition(),
                        command.getLength(),
                        command.getClientVersion()
                );
            }
            case RETAIN -> TextOperation.retain(
                    userContext.getUid(),
                    command.getPosition(),
                    command.getLength() != null ? command.getLength() : 0,
                    command.getClientVersion()
            );
        };
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
