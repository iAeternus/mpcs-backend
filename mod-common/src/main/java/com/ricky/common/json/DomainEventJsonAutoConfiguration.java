package com.ricky.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricky.common.event.DomainEventSubtypeRegistrar;
import com.ricky.common.utils.MyObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;


/**
 * @author Ricky
 * @version 1.0
 * @date 2025/3/16
 * @className DomainEventJsonConfiguration
 * @desc
 */
@AutoConfigureAfter(JsonAutoConfiguration.class)
public class DomainEventJsonAutoConfiguration {

    @Bean
    public ObjectMapper domainEventSubtypeRegistrarInvoker(MyObjectMapper mapper, ObjectProvider<DomainEventSubtypeRegistrar> registrars) {
        registrars.orderedStream()
                .forEach(r -> r.register(mapper));
        return mapper;
    }
//    private static final Reflections REFLECTIONS = new Reflections("com.baeldung.jackson.polymorphicdeserialization.reflection");
//
//    @Bean("domainEventObjectMapper")
//    public ObjectMapper objectMapper() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        // 获取本模块下所有DomainEvent子类
//        Set<Class<? extends DomainEvent>> classes = REFLECTIONS.getSubTypesOf(DomainEvent.class);
//        if (isEmpty(classes)) {
//            return objectMapper;
//        }
//
//        for (Class<? extends DomainEvent> clazz : classes) {
//            // 跳过接口和抽象类
//            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
//                continue;
//            }
//            // 提取 JsonTypeDefine 注解
//            JsonTypeDefine extendClassDefine = clazz.getAnnotation(JsonTypeDefine.class);
//            if (isNull(extendClassDefine)) {
//                continue;
//            }
//            // 注册子类型，使用名称建立关联
//            objectMapper.registerSubtypes(new NamedType(clazz, extendClassDefine.value()));
//        }
//
//        return objectMapper;
//    }

}
