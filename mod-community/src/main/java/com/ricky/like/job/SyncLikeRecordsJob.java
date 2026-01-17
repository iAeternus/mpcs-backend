package com.ricky.like.job;

import com.ricky.like.domain.*;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.constants.ConfigConstants.DATE_TIME_FORMATTER;
import static com.ricky.common.domain.user.UserContext.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncLikeRecordsJob {

    private final CachedLikeRepository cachedLikeRepository;
    private final LikeRepository likeRepository;
    private final PublicFileRepository publicFileRepository;

    public void run(LocalDateTime time) {
        LocalDateTime startTime = time.withMinute(0).withSecond(0).withNano(0);
        String timeString = DATE_TIME_FORMATTER.format(startTime);
        log.info("Started sync like record at [{}].", timeString);

        List<LikeRecord> likeRecords = cachedLikeRepository.listAllLike();
        List<LikedCount> likedCounts = cachedLikeRepository.listAllLikedCount();

        // 落库Like聚合根
        List<Like> likes = likeRecords.stream()
                .map(record -> new Like(record, NOUSER))
                .collect(toImmutableList());
        likeRepository.save(likes);

        // 同步PublicFile聚合根
        Map<String, Integer> likeCountsMap = likedCounts.stream()
                .collect(Collectors.toMap(LikedCount::getPostId, LikedCount::getCount));
        List<PublicFile> publicFiles = publicFileRepository.byIds(likeCountsMap.keySet());
        publicFiles.forEach(pf -> pf.updateLikeCount(likeCountsMap.getOrDefault(pf.getId(), 0)));
        publicFileRepository.save(publicFiles);
    }

}
