package com.ricky.collaboration.collaboration.domain;

import lombok.*;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(force = true)
public class CollabUser {

    private String oderId;
    private String username;
    private String avatarUrl;
    private Instant joinedAt;
    private Instant lastActiveAt;
    private boolean online;

    public static CollabUser create(String userId, String username) {
        Instant now = Instant.now();
        return CollabUser.builder()
                .oderId(userId)
                .username(username)
                .avatarUrl(null)
                .joinedAt(now)
                .lastActiveAt(now)
                .online(true)
                .build();
    }

    public CollabUser withOnline(boolean isOnline) {
        return CollabUser.builder()
                .oderId(oderId)
                .username(username)
                .avatarUrl(avatarUrl)
                .joinedAt(joinedAt)
                .lastActiveAt(isOnline ? Instant.now() : lastActiveAt)
                .online(isOnline)
                .build();
    }

    public CollabUser updateActivity() {
        return CollabUser.builder()
                .oderId(oderId)
                .username(username)
                .avatarUrl(avatarUrl)
                .joinedAt(joinedAt)
                .lastActiveAt(Instant.now())
                .online(online)
                .build();
    }

    public CollabUser withAvatar(String url) {
        return CollabUser.builder()
                .oderId(oderId)
                .username(username)
                .avatarUrl(url)
                .joinedAt(joinedAt)
                .lastActiveAt(lastActiveAt)
                .online(online)
                .build();
    }
}
