package com.ricky.collaboration.collaboration.domain;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(force = true)
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
        return increment().withLength(documentLength);
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
