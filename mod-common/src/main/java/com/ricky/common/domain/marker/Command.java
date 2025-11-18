package com.ricky.common.domain.marker;

/**
 * 命令入参 marker
 * 适用于增、删、改接口，用于实现CQRS
 * 规范：<br>
 * 1. 实现类非必要不可变<br>
 * 2. 实现类必须支持建造者模式<br>
 * 3. 实现类必须将全参构造私有<br>
 */
public interface Command {

    default void correctAndValidate() {
    }

}
