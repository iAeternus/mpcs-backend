package com.ricky.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className TaskRunner
 * @desc 任务运行器
 */
@Slf4j
public class TaskRunner {

    public static void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            log.error("Failed to run task: ", t);
        }
    }

}
