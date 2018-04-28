package com.nng.unit;

import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 事件总线.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventBusInstance {
    
    private static final EventBus INSTANCE = new EventBus();
    
    /**
     * 获取事件总线实例.
     * 
     * @return 事件总线实例
     */
    public static EventBus getInstance() {
        return INSTANCE;
    }
}
