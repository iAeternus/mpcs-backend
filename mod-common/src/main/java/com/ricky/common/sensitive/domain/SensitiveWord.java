package com.ricky.common.sensitive.domain;

import com.ricky.common.domain.marker.Identified;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.SENSITIVE_WORD_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.SENSITIVE_WORD_ID_PREFIX;

@Getter
@Document(SENSITIVE_WORD_COLLECTION)
@TypeAlias(SENSITIVE_WORD_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SensitiveWord implements Identified {

    private String id;
    private String word;

    public SensitiveWord(String word) {
        this.id = newSensitiveWordId();
        this.word = word;
    }

    public static String newSensitiveWordId() {
        return SENSITIVE_WORD_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

}
