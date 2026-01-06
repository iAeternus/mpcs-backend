package com.ricky.common.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className RedisProperties
 */
@Data
@Validated
public class RedisProperties {

    @NotNull
    private Boolean domainEventStreamEnabled;

    @NotBlank
    private String domainEventStreamPrefix;

    @Min(value = 1)
    @Max(value = 10)
    private int domainEventStreamCount;


    @NotBlank
    private String notificationStream;

    @NotBlank
    private String webhookStream;

    public List<String> allDomainEventStreams() {
        return IntStream.range(1, this.domainEventStreamCount + 1).mapToObj(this::domainEventStreamOfIndex).toList();
    }

    public String domainEventStreamForUser(String userId) {
        if (isBlank(userId)) {
            return this.domainEventStreamOfIndex(1);
        }

        int index = Math.abs(userId.hashCode() % this.domainEventStreamCount) + 1;
        return this.domainEventStreamOfIndex(index);
    }

    private String domainEventStreamOfIndex(int index) {
        return this.domainEventStreamPrefix + "." + index;
    }


}
