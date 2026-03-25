package com.ricky.collaboration.domain;

import com.ricky.collaboration.domain.ot.TextOperation;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SessionVersion {

    private long version;
    private long documentLength;

    public static SessionVersion initial() {
        return new SessionVersion(0L, 0L);
    }

    public static SessionVersion of(long version, long documentLength) {
        return new SessionVersion(version, documentLength);
    }

    public SessionVersion increment() {
        return new SessionVersion(version + 1, documentLength);
    }

    public SessionVersion increment(long delta) {
        return new SessionVersion(version + delta, documentLength);
    }

    public SessionVersion withLength(long newLength) {
        return new SessionVersion(version, newLength);
    }

    public SessionVersion next() {
        return increment();
    }

    public SessionVersion applyOperation(TextOperation operation) {
        long newLength = documentLength;
        if (operation.isInsert()) {
            newLength += operation.getLength();
        } else if (operation.isDelete()) {
            newLength -= operation.getLength();
        }
        SessionVersion result = new SessionVersion(version + 1, Math.max(0, newLength));
        return result;
    }

    public boolean isAheadOf(long clientVersion) {
        return version > clientVersion;
    }

    public boolean isAt(long clientVersion) {
        return version == clientVersion;
    }

    public boolean isBehind(long clientVersion) {
        return version < clientVersion;
    }
}
