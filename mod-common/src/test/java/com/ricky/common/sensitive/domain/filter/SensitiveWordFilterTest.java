package com.ricky.common.sensitive.domain.filter;

import com.ricky.common.sensitive.domain.filter.impl.ACFilter;
import com.ricky.common.sensitive.domain.filter.impl.ACProFilter;
import com.ricky.common.sensitive.domain.filter.impl.DFAFilter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordFilterTest {

    @Test
    void should_return_false_when_no_sensitive_word() {
        // Given
        List<String> sensitiveList = Arrays.asList("abcdb", "abcbba", "adabca");
        DFAFilter dfaFilter = DFAFilter.getInstance();
        dfaFilter.loadWord(sensitiveList);
        ACFilter acFilter = ACFilter.getInstance();
        acFilter.loadWord(sensitiveList);
        ACProFilter acProFilter = ACProFilter.getInstance();
        acProFilter.loadWord(sensitiveList);

        // When
        boolean dfaRes = dfaFilter.hasSensitiveWord("adabcd");
        boolean acRes = acFilter.hasSensitiveWord("adabcd");
        boolean acProRes = acProFilter.hasSensitiveWord("adabcd");

        // Then
        assertFalse(dfaRes);
        assertFalse(acRes);
        assertFalse(acProRes);
    }

    @Test
    void test_dfa_filter() {
        // Given
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);

        // When
        boolean res = instance.hasSensitiveWord("adabcd");

        // Then
        assertTrue(res);
    }

    @Test
    void test_ac_filter() {
        // Given
        List<String> sensitiveList = Arrays.asList("abcd", "abcbba", "adabca");
        ACFilter instance = ACFilter.getInstance();
        instance.loadWord(sensitiveList);

        // When
        boolean res = instance.hasSensitiveWord("adabcd");

        // Then
        assertTrue(res);
    }

    @Test
    void test_ac_pro_filter() {
        // Given
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        ACProFilter acProFilter = ACProFilter.getInstance();
        acProFilter.loadWord(sensitiveList);

        // When
        String res = acProFilter.filter("你是白痴吗");

        // Then
        assertEquals("*****", res);
    }

    @Test
    void test_dfa_filter_multiple_matches() {
        // Given
        List<String> sensitiveList = Arrays.asList("白痴", "你是白痴", "白痴吗");
        DFAFilter instance = DFAFilter.getInstance();
        instance.loadWord(sensitiveList);

        // When
        String res = instance.filter("你是白痴吗");

        // Then
        assertEquals("****吗", res);
    }

    @Test
    void test_ac_filter_multiple_matches() {
        // Given
        List<String> sensitiveList = Arrays.asList("你是白痴", "你是");
        ACFilter instance = ACFilter.getInstance();
        instance.loadWord(sensitiveList);

        // When
        String res = instance.filter("你是白痴吗");

        // Then
        assertEquals("**白痴吗", res);
    }

}