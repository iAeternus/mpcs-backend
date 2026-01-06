package com.ricky.upload.domain.tasks.extracttext;

import com.ricky.file.domain.FileCategory;
import com.ricky.upload.domain.tasks.extracttext.impl.AbstractTextExtractor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextExtractorFactory {

    private final List<TextExtractor> extractors;
    private Map<FileCategory, TextExtractor> extractorCache;

    @PostConstruct
    public void init() {
        extractorCache = extractors.stream()
                .filter(extractor -> extractor instanceof AbstractTextExtractor)
                .collect(Collectors.toMap(
                        extractor -> ((AbstractTextExtractor) extractor).getSupportedCategory(),
                        Function.identity()
                ));
        log.info("Loaded {} text extractors: {}", extractorCache.size(), extractorCache.keySet());
    }

    /**
     * 获取支持指定分类的提取器
     */
    public Optional<TextExtractor> getExtractor(FileCategory category) {
        return Optional.ofNullable(extractorCache.get(category));
    }

    /**
     * 检查是否有提取器支持该分类
     */
    public boolean supports(FileCategory category) {
        return extractorCache.containsKey(category);
    }

}
