package com.ricky.collaboration.collaboration.domain.ot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class OperationComponent {

    private final TextOperationType type;
    private final int position;
    private final String content;
    private final int length;

    public static OperationComponent insert(int position, String content) {
        return new OperationComponent(TextOperationType.INSERT, position, content, content.length());
    }

    public static OperationComponent delete(int position, int length) {
        return new OperationComponent(TextOperationType.DELETE, position, null, length);
    }

    public static OperationComponent retain(int position, int length) {
        return new OperationComponent(TextOperationType.RETAIN, position, null, length);
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
