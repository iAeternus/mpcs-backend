package com.ricky.common.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@JsonSubTypes({
        // 如果有其他子类，在这里注册
        // @JsonSubTypes.Type(value = SomeSubclass.class, name = "someSubclass")
})
public abstract class OpenAiChatOptionsMixin {
    // 不需要具体实现，只用于注解
}