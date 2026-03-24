package com.ricky.collaboration.collaboration.domain;

import com.ricky.collaboration.collaboration.domain.event.SessionCreatedEvent;
import com.ricky.collaboration.collaboration.domain.event.SessionDeletedEvent;
import com.ricky.collaboration.collaboration.domain.event.UserJoinedEvent;
import com.ricky.collaboration.collaboration.domain.event.UserLeftEvent;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.common.constants.ConfigConstants;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;

import static com.ricky.common.constants.ConfigConstants.COLLABORATION_SESSION_COLLECTION;
import static lombok.AccessLevel.PROTECTED;

@Getter
@FieldNameConstants
@Document(COLLABORATION_SESSION_COLLECTION)
@TypeAlias("collab_session")
@NoArgsConstructor(access = PROTECTED)
public class CollaborationSession extends AggregateRoot {

    private static final int MAX_USERS = 100;

    private String documentId;
    private String documentTitle;
    private String parentFolderId;
    private SessionVersion version;
    private Long baseVersion;
    private Set<CollabUser> activeUsers;
    private Map<String, CursorPosition> cursors;
    private List<TextOperation> operationHistory;
    private Instant lastActivityAt;
    private Instant expiresAt;

    public CollaborationSession(
            String documentId,
            String documentTitle,
            String parentFolderId,
            UserContext userContext,
            long ttlHours
    ) {
        super(newSessionId(), userContext);
        init(documentId, documentTitle, parentFolderId, userContext, ttlHours);
    }

    private void init(
            String documentId,
            String documentTitle,
            String parentFolderId,
            UserContext userContext,
            long ttlHours
    ) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.parentFolderId = parentFolderId;
        this.version = SessionVersion.initial();
        this.baseVersion = 0L;
        this.activeUsers = new HashSet<>();
        this.cursors = new HashMap<>();
        this.operationHistory = new ArrayList<>();
        this.lastActivityAt = Instant.now();
        this.expiresAt = this.lastActivityAt.plusSeconds(ttlHours * 3600);

        CollabUser owner = CollabUser.create(userContext.getUid(), userContext.getUsername());
        this.activeUsers.add(owner);
        this.cursors.put(userContext.getUid(), CursorPosition.of(userContext.getUid(), userContext.getUsername(), 0));

        addOpsLog("创建协同会话", userContext);
    }

    public static String newSessionId() {
        return ConfigConstants.COLLAB_SESSION_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public boolean join(UserContext userContext) {
        if (activeUsers.size() >= MAX_USERS) {
            return false;
        }

        CollabUser newUser = CollabUser.create(userContext.getUid(), userContext.getUsername());
        if (activeUsers.stream().anyMatch(u -> u.getOderId().equals(userContext.getUid()))) {
            return false;
        }

        this.activeUsers.add(newUser);
        this.cursors.put(userContext.getUid(), CursorPosition.of(userContext.getUid(), userContext.getUsername(), 0));
        this.lastActivityAt = Instant.now();

        addOpsLog("用户[" + userContext.getUsername() + "]加入会话", userContext);
        return true;
    }

    public boolean leave(String oderId, UserContext userContext) {
        boolean removed = this.activeUsers.removeIf(u -> u.getOderId().equals(oderId));
        if (removed) {
            this.cursors.remove(oderId);
            this.lastActivityAt = Instant.now();
            addOpsLog("用户[" + oderId + "]离开会话", userContext);
        }
        return removed;
    }

    public boolean isUserInSession(String oderId) {
        return activeUsers.stream().anyMatch(u -> u.getOderId().equals(oderId));
    }

    public void updateCursor(String oderId, CursorPosition cursor, UserContext userContext) {
        if (!isUserInSession(oderId)) {
            return;
        }
        this.cursors.put(oderId, cursor.withTimestamp());
        this.lastActivityAt = Instant.now();
    }

    public void addOperation(TextOperation operation, UserContext userContext) {
        this.version = version.applyOperation(operation);
        this.operationHistory.add(operation);
        this.lastActivityAt = Instant.now();
        addOpsLog("用户[" + operation.getUserId() + "]提交操作[" + operation.getType() + "@" + operation.getPosition() + "]", userContext);
    }

    public void updateBaseVersion(long newBaseVersion, UserContext userContext) {
        if (newBaseVersion > this.baseVersion) {
            this.baseVersion = newBaseVersion;
            this.lastActivityAt = Instant.now();
            addOpsLog("更新基准版本 to " + newBaseVersion, userContext);
        }
    }

    public void updateUserActivity(String oderId) {
        this.activeUsers.stream()
                .filter(u -> u.getOderId().equals(oderId))
                .findFirst()
                .ifPresent(CollabUser::updateActivity);
        this.lastActivityAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isEmpty() {
        return activeUsers.isEmpty();
    }

    public boolean isFull() {
        return activeUsers.size() >= MAX_USERS;
    }

    public int getActiveUserCount() {
        return activeUsers.size();
    }

    public List<TextOperation> getRecentOperations(int limit) {
        if (operationHistory.size() <= limit) {
            return new ArrayList<>(operationHistory);
        }
        return new ArrayList<>(operationHistory.subList(operationHistory.size() - limit, operationHistory.size()));
    }

    public List<TextOperation> getOperationsSince(long fromVersion) {
        if (fromVersion >= version.getVersion()) {
            return Collections.emptyList();
        }
        return operationHistory.stream()
                .filter(op -> op.getClientVersion() >= fromVersion)
                .toList();
    }

    public void raiseSessionCreatedEvent(String documentId, String documentTitle, UserContext userContext) {
        raiseEvent(new SessionCreatedEvent(getId(), documentId, documentTitle, userContext));
    }

    public void raiseUserJoinedEvent(String sessionId, String oderId, String username, UserContext userContext) {
        raiseEvent(new UserJoinedEvent(sessionId, oderId, username, userContext));
    }

    public void raiseUserLeftEvent(String sessionId, String oderId, UserContext userContext) {
        raiseEvent(new UserLeftEvent(sessionId, oderId, userContext));
    }

    public void raiseSessionDeletedEvent(String sessionId, UserContext userContext) {
        raiseEvent(new SessionDeletedEvent(sessionId, userContext));
    }
}
