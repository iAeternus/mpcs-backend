package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ricky.common.utils.CommonUtils.singleParameterizedArgumentClassOf;
import static java.util.Comparator.comparingInt;

/**
 * 处理领域事件的入口点，它会查找所有符合条件的处理器并依次调用它们
 * 当多个处理器都符合处理某个领域事件的条件时，每个处理器都会独立完成自己的工作
 * 不会受到其他处理器抛出的异常的影响
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventConsumer<T extends DomainEvent> {

    private final Map<String, Class<?>> handlerEventClassMap = new ConcurrentHashMap<>();
    private final List<DomainEventHandler<T>> handlers;

    public void consume(ConsumingDomainEvent<T> consumingDomainEvent) {
        log.debug("Start consume domain event[{}:{}].", consumingDomainEvent.getType(), consumingDomainEvent.getEventId());
        handlers.stream()
                .filter(handler -> canHandle(handler, consumingDomainEvent.getEvent()))
                .sorted(comparingInt(DomainEventHandler::priority))
                .forEach(handler -> {
                    try {
                        handler.handle(consumingDomainEvent);
                    } catch (Throwable t) {
                        log.error("Error occurred while handling domain event[{}:{}] by {}.",
                                consumingDomainEvent.getType(), consumingDomainEvent.getEventId(), handler.getClass().getName(), t);
                    }
                });
    }

    private boolean canHandle(DomainEventHandler<T> handler, T event) {
        String handlerClassName = handler.getClass().getName();

        if (!handlerEventClassMap.containsKey(handlerClassName)) {
            Class<?> handlerEventClass = singleParameterizedArgumentClassOf(handler.getClass());
            handlerEventClassMap.put(handlerClassName, handlerEventClass);
        }

        Class<?> finalHandlerEventClass = handlerEventClassMap.get(handlerClassName);
        return finalHandlerEventClass != null && finalHandlerEventClass.isAssignableFrom(event.getClass());
    }

}
