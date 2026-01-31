package com.ricky.common.sensitive.service.impl;

import com.ricky.common.sensitive.SensitiveWordProperties;
import com.ricky.common.sensitive.domain.SensitiveWord;
import com.ricky.common.sensitive.domain.SensitiveWordRepository;
import com.ricky.common.sensitive.domain.filter.SensitiveWordFilter;
import com.ricky.common.sensitive.service.SensitiveWordService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordFilter filter;
    private SensitiveWordRepository repository;

    private SensitiveWordServiceImpl(SensitiveWordProperties properties) {
        this.filter = properties.getAlgorithm().getFilter();
    }

    public static SensitiveWordServiceImpl newInstance(SensitiveWordProperties properties) {
        return new SensitiveWordServiceImpl(properties);
    }

    public SensitiveWordServiceImpl repository(SensitiveWordRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Sensitive word repository can not be null");
        }
        this.repository = repository;
        return this;
    }

    public SensitiveWordService init() {
        List<String> words = repository.findAll().stream()
                .map(SensitiveWord::getWord)
                .collect(toImmutableList());
        filter.loadWord(words);
        return this;
    }

    @Override
    public boolean hasSensitiveWord(String text) {
        return filter.hasSensitiveWord(text);
    }

    @Override
    public boolean hasSensitiveWord(InputStream stream) throws IOException {
        return filter.hasSensitiveWord(stream);
    }

    @Override
    public String filter(String text) {
        return filter.filter(text);
    }

    @Override
    public String filter(InputStream stream) throws IOException {
        return filter.filter(stream);
    }
}
