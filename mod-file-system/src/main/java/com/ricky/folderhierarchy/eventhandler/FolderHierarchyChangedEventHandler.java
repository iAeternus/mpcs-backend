package com.ricky.folderhierarchy.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import org.springframework.stereotype.Component;

@Component
public class FolderHierarchyChangedEventHandler extends AbstractDomainEventHandler<FolderHierarchyChangedEvent> {
    @Override
    protected void doHandle(FolderHierarchyChangedEvent event) {
        // TODO 同步权限组（梳理权限继承逻辑）
    }
}
