package com.ricky.common.validation.path;


import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.common.constants.RegexConstants.PATH_PATTERN;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidatorTest {

    static Pattern PATTERN = Pattern.compile(PATH_PATTERN);

    @Test
    void should_path_pattern_match() {
        // Given
        String path1 = "";                     // 空字符串（根目录）
        String path2 = "home";                 // 单级路径
        String path3 = "usr/local/bin";        // 多级路径
        String path4 = "folder-1";             // 包含连字符
        String path5 = "my_doc";               // 包含下划线
        String path6 = "a1-b2_c3/d4-e5_f6";    // 混合字符

        // When & Then
        assertTrue(PATTERN.matcher(path1).matches());
        assertTrue(PATTERN.matcher(path2).matches());
        assertTrue(PATTERN.matcher(path3).matches());
        assertTrue(PATTERN.matcher(path4).matches());
        assertTrue(PATTERN.matcher(path5).matches());
        assertTrue(PATTERN.matcher(path6).matches());
    }

    @Test
    void should_path_pattern_not_match() {
        // Given
        String path1 = "/";                    // 单斜杠
        String path2 = "/home";                // 前导斜杠
        String path3 = "/usr/local/bin";       // 前导斜杠多级路径
        String path4 = "home/";                // 结尾斜杠
        String path5 = "path//double";         // 连续斜杠
        String path6 = "path with spaces";     // 包含空格
        String path7 = "path@home";            // 特殊字符@
        String path8 = "path#hash";            // 特殊字符#
        String path9 = "path.";                // 结尾点号
        String path10 = ".hidden";             // 前导点号

        // When & Then
        assertFalse(PATTERN.matcher(path1).matches());
        assertFalse(PATTERN.matcher(path2).matches());
        assertFalse(PATTERN.matcher(path3).matches());
        assertFalse(PATTERN.matcher(path4).matches());
        assertFalse(PATTERN.matcher(path5).matches());
        assertFalse(PATTERN.matcher(path6).matches());
        assertFalse(PATTERN.matcher(path7).matches());
        assertFalse(PATTERN.matcher(path8).matches());
        assertFalse(PATTERN.matcher(path9).matches());
        assertFalse(PATTERN.matcher(path10).matches());
    }

}