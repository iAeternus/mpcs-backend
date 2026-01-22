package com.ricky.common.schedule;

import com.ricky.comment.job.SyncCommentCountsJob;
import com.ricky.common.event.DomainEventJobs;
import com.ricky.common.event.publish.DomainEventPublisher;
import com.ricky.like.job.SyncLikeRecordsJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static java.time.LocalDateTime.now;

@Slf4j
//@NonCiProfile
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "60m", defaultLockAtLeastFor = "10s")
public class SchedulingConfiguration {

    private final DomainEventPublisher domainEventPublisher;
    private final DomainEventJobs domainEventJobs;
    private final SyncLikeRecordsJob syncLikeRecordsJob;
    private final SyncCommentCountsJob syncCommentCountsJob;

    // 定时任务尽量放到前半个小时运行，以将后半个多小时留给部署时间

    // 兜底发送尚未发送的事件，每2分钟运行，不能用@SchedulerLock，因为publishDomainEvents本身有分布式锁
    @Scheduled(cron = "0 */2 * * * ?")
    public void houseKeepPublishDomainEvent() {
        int count = domainEventPublisher.publishStagedDomainEvents();
        if (count > 0) {
            log.debug("House keep published {} domain events.", count);
        }
    }

    // 将点赞记录同步至数据库，每小时第1分钟运行，不能整点，因为整点有可能会被计算成上一小时
    @Scheduled(cron = "0 1 */1 * * ?")
    @SchedulerLock(name = "syncLikeRecords", lockAtMostFor = "40m", lockAtLeastFor = "1m")
    public void syncLikeRecords() {
        syncLikeRecordsJob.run(now());
    }

    // 将评论数同步至数据库，每小时第5分钟运行，为了和syncLikeRecords错开
    @Scheduled(cron = "0 4 */1 * * ?")
    @SchedulerLock(name = "syncCommentCounts", lockAtMostFor = "40m", lockAtLeastFor = "1m")
    public void syncCommentCounts() {
        syncCommentCountsJob.run(now());
    }

    // add here...

    // 删除老的领域事件，包含mongo和redis，每季度第一天3点20分运行
    @Scheduled(cron = "0 20 3 1 1,4,7,10 ?")
    @SchedulerLock(name = "removeOldEvents", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void removeOldEvents() {
        try {
            domainEventJobs.removeOldPublishingDomainEventsFromMongo(100);
        } catch (Throwable t) {
            log.error("Failed remove old publishing domain events from mongo.", t);
        }

        try {
            domainEventJobs.removeOldConsumingDomainEventsFromMongo(100);
        } catch (Throwable t) {
            log.error("Failed remove old consuming domain events from mongo.", t);
        }

        try {
            domainEventJobs.removeOldDomainEventsFromRedis(1000000, true);
        } catch (Throwable t) {
            log.error("Failed remove old domain events from redis.", t);
        }

        try {
            domainEventJobs.removeOldWebhookEventsFromRedis(1000000, true);
        } catch (Throwable t) {
            log.error("Failed remove old webhook events from redis.", t);
        }

        try {
            domainEventJobs.removeOldNotificationEventsFromRedis(500000, true);
        } catch (Throwable t) {
            log.error("Failed remove old notification events from redis.", t);
        }
    }

}
