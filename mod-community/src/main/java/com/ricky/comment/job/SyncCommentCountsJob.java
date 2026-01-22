package com.ricky.comment.job;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.ricky.common.constants.ConfigConstants.DATE_TIME_FORMATTER;
import static com.ricky.common.domain.user.UserContext.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCommentCountsJob {

    private final CachedCommentRepository cachedCommentRepository;
    private final PublicFileRepository publicFileRepository;

    public void run(LocalDateTime time) {
        LocalDateTime startTime = time.withMinute(0).withSecond(0).withNano(0);
        String timeString = DATE_TIME_FORMATTER.format(startTime);
        log.info("Started sync comment count at [{}].", timeString);

        Map<String, Integer> commentCounts = cachedCommentRepository.listAllCommentCount();

        List<PublicFile> publicFiles = publicFileRepository.byIds(commentCounts.keySet());
        publicFiles.forEach(publicFile ->
                publicFile.updateCommentCount(commentCounts.get(publicFile.getId()), NOUSER));
        publicFileRepository.save(publicFiles);
    }

}
