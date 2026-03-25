package com.ricky.collaboration.domain.ot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TextOperationType {
    INSERT(1),
    DELETE(2),
    RETAIN(3);

    private final int code;

    public static TextOperationType fromCode(int code) {
        for (TextOperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown operation type code: " + code);
    }
}
