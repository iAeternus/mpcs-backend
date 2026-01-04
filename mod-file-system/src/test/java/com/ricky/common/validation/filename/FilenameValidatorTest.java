package com.ricky.common.validation.filename;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.common.constants.RegexConstants.FILENAME_PATTERN;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilenameValidatorTest {

    private static final Pattern PATTERN = Pattern.compile(FILENAME_PATTERN);

    @Test
    void should_filename_pattern_match_valid_filenames() {
        // Given - 标准文件名
        String fileName1 = "document.pdf";
        String fileName2 = "photo.jpg";
        String fileName3 = "data_2024.xlsx";
        String fileName4 = "my_file.txt";
        String fileName5 = "test-123.json";

        // Given - 带数字的文件名
        String fileName6 = "image001.png";
        String fileName7 = "version2.0.pdf";
        String fileName8 = "2024-01-01-report.csv";

        // Given - 各种扩展名长度
        String fileName9 = "a.b";                     // 最短扩展名
        String fileName10 = "file.extension";         // 9字符扩展名
        String fileName11 = "file.extension0";        // 10字符扩展名（最大长度）

        // When & Then
        assertTrue(PATTERN.matcher(fileName1).matches(), "标准PDF文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName2).matches(), "标准JPG文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName3).matches(), "带下划线的Excel文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName4).matches(), "带下划线的文本文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName5).matches(), "带连字符的JSON文件名应该匹配");

        assertTrue(PATTERN.matcher(fileName6).matches(), "带数字的图片文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName7).matches(), "带版本号的文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName8).matches(), "带日期的文件名应该匹配");

        assertTrue(PATTERN.matcher(fileName9).matches(), "最短扩展名应该匹配");
        assertTrue(PATTERN.matcher(fileName10).matches(), "9字符扩展名应该匹配");
        assertTrue(PATTERN.matcher(fileName11).matches(), "10字符扩展名应该匹配");
    }

    @Test
    void should_filename_pattern_not_match_invalid_filenames() {
        // Given - 缺少扩展名
        String fileName1 = "document";
        String fileName2 = "file.";                    // 扩展名为空

        // Given - 扩展名过长
        String fileName3 = "file.verylongextension";   // 扩展名13字符（超过10个）
        String fileName4 = "file.12345678901";         // 扩展名11字符

        // Given - 包含非法字符
        String fileName5 = "file/name.txt";           // 包含正斜杠
        String fileName6 = "file\\name.txt";          // 包含反斜杠
        String fileName7 = "file:name.txt";           // 包含冒号
        String fileName8 = "file*name.txt";           // 包含星号
        String fileName9 = "file?name.txt";           // 包含问号
        String fileName10 = "file\"name.txt";         // 包含双引号
        String fileName11 = "file<name.txt";          // 包含小于号
        String fileName12 = "file>name.txt";          // 包含大于号
        String fileName13 = "file|name.txt";          // 包含管道符

        // Given - 控制字符
        String fileName14 = "file\nname.txt";         // 包含换行符
        String fileName15 = "file\tname.txt";         // 包含制表符
        String fileName16 = "file\rname.txt";         // 包含回车符

        // Given - 扩展名包含非法字符
        String fileName17 = "file.t@xt";              // 扩展名包含@
        String fileName18 = "file.te#t";              // 扩展名包含#
        String fileName19 = "file.te$t";              // 扩展名包含$
        String fileName20 = "file.te%t";              // 扩展名包含%
        String fileName21 = "file.te^t";              // 扩展名包含^
        String fileName22 = "file.te&t";              // 扩展名包含&
        String fileName23 = "file.te(t";              // 扩展名包含(
        String fileName24 = "file.te)t";              // 扩展名包含)
        String fileName25 = "file.te+t";              // 扩展名包含+
        String fileName26 = "file.te=t";              // 扩展名包含=

        // Given - 文件名主体为空
        String fileName27 = ".gitignore";             // 只有扩展名
        String fileName28 = ".env";                   // 隐藏文件

        // When & Then
        // 缺少扩展名
        assertFalse(PATTERN.matcher(fileName1).matches());
        assertFalse(PATTERN.matcher(fileName2).matches());

        // 扩展名过长
        assertFalse(PATTERN.matcher(fileName3).matches());
        assertFalse(PATTERN.matcher(fileName4).matches());

        // 非法字符
        assertFalse(PATTERN.matcher(fileName5).matches());
        assertFalse(PATTERN.matcher(fileName6).matches());
        assertFalse(PATTERN.matcher(fileName7).matches());
        assertFalse(PATTERN.matcher(fileName8).matches());
        assertFalse(PATTERN.matcher(fileName9).matches());
        assertFalse(PATTERN.matcher(fileName10).matches());
        assertFalse(PATTERN.matcher(fileName11).matches());
        assertFalse(PATTERN.matcher(fileName12).matches());
        assertFalse(PATTERN.matcher(fileName13).matches());

        // 控制字符
        assertFalse(PATTERN.matcher(fileName14).matches());
        assertFalse(PATTERN.matcher(fileName15).matches());
        assertFalse(PATTERN.matcher(fileName16).matches());

        // 扩展名非法字符
        assertFalse(PATTERN.matcher(fileName17).matches());
        assertFalse(PATTERN.matcher(fileName18).matches());
        assertFalse(PATTERN.matcher(fileName19).matches());
        assertFalse(PATTERN.matcher(fileName20).matches());
        assertFalse(PATTERN.matcher(fileName21).matches());
        assertFalse(PATTERN.matcher(fileName22).matches());
        assertFalse(PATTERN.matcher(fileName23).matches());
        assertFalse(PATTERN.matcher(fileName24).matches());
        assertFalse(PATTERN.matcher(fileName25).matches());
        assertFalse(PATTERN.matcher(fileName26).matches());

        // 文件名主体为空
        assertFalse(PATTERN.matcher(fileName27).matches());
        assertFalse(PATTERN.matcher(fileName28).matches());
    }

    @Test
    void should_filename_pattern_match_filename_with_special_characters_in_name() {
        // Given - 文件名主体包含特殊字符（允许的字符）
        String fileName1 = "my file.txt";            // 包含空格
        String fileName2 = "file[1].txt";            // 包含方括号
        String fileName3 = "file(1).txt";            // 包含圆括号
        String fileName4 = "file@1.txt";             // 包含@
        String fileName5 = "file#1.txt";             // 包含#
        String fileName6 = "file$1.txt";             // 包含$
        String fileName7 = "file%1.txt";             // 包含%
        String fileName8 = "file&1.txt";             // 包含&
        String fileName9 = "file'1.txt";             // 包含单引号
        String fileName10 = "file+1.txt";            // 包含+
        String fileName11 = "file=1.txt";            // 包含=
        String fileName12 = "file~1.txt";            // 包含~
        String fileName13 = "file`1.txt";            // 包含`
        String fileName14 = "file!1.txt";            // 包含!
        String fileName15 = "file@home.txt";         // 包含@（多个）

        // When & Then
        assertTrue(PATTERN.matcher(fileName1).matches(), "包含空格应该匹配");
        assertTrue(PATTERN.matcher(fileName2).matches(), "包含方括号应该匹配");
        assertTrue(PATTERN.matcher(fileName3).matches(), "包含圆括号应该匹配");
        assertTrue(PATTERN.matcher(fileName4).matches(), "包含@应该匹配");
        assertTrue(PATTERN.matcher(fileName5).matches(), "包含#应该匹配");
        assertTrue(PATTERN.matcher(fileName6).matches(), "包含$应该匹配");
        assertTrue(PATTERN.matcher(fileName7).matches(), "包含%应该匹配");
        assertTrue(PATTERN.matcher(fileName8).matches(), "包含&应该匹配");
        assertTrue(PATTERN.matcher(fileName9).matches(), "包含单引号应该匹配");
        assertTrue(PATTERN.matcher(fileName10).matches(), "包含+应该匹配");
        assertTrue(PATTERN.matcher(fileName11).matches(), "包含=应该匹配");
        assertTrue(PATTERN.matcher(fileName12).matches(), "包含~应该匹配");
        assertTrue(PATTERN.matcher(fileName13).matches(), "包含`应该匹配");
        assertTrue(PATTERN.matcher(fileName14).matches(), "包含!应该匹配");
        assertTrue(PATTERN.matcher(fileName15).matches(), "包含多个@应该匹配");
    }

    @Test
    void should_filename_pattern_match_international_filenames() {
        // Given - 国际化文件名
        String fileName1 = "文件.txt"; // 中文
        String fileName2 = "ドキュメント.pdf"; // 日文
        String fileName3 = "파일.txt"; // 韩文
        String fileName4 = "файл.txt"; // 俄文
        String fileName5 = "αρχείο.txt"; // 希腊文
        String fileName6 = "ملف.txt"; // 阿拉伯文
        String fileName7 = "ไฟล์.txt"; // 泰文
        String fileName8 = "file-测试.txt"; // 中英文混合
        String fileName9 = "résumé.pdf"; // 带重音符号
        String fileName10 = "café.jpg"; // 特殊字符é
        String fileName11 = "naïve.txt"; // 特殊字符ï

        // When & Then
        assertTrue(PATTERN.matcher(fileName1).matches(), "中文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName2).matches(), "日文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName3).matches(), "韩文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName4).matches(), "俄文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName5).matches(), "希腊文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName6).matches(), "阿拉伯文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName7).matches(), "泰文文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName8).matches(), "中英文混合文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName9).matches(), "带重音符号的文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName10).matches(), "包含é的文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName11).matches(), "包含ï的文件名应该匹配");
    }

    @Test
    void should_filename_pattern_match_edge_cases() {
        // Given - 边界情况
        String fileName1 = "a.b";                      // 最短可能文件名
        String fileName2 = "ab.c";                     // 短文件名
        String fileName3 = "1.2";                      // 纯数字文件名
        String fileName4 = "a.1";                      // 字母扩展名是数字
        String fileName5 = "1.a";                      // 数字文件名主体

        // 长文件名主体（但扩展名合法）
        String longName = "a".repeat(100);
        String fileName6 = longName + ".txt";

        // 混合大小写扩展名
        String fileName7 = "file.PDF";
        String fileName8 = "file.JPG";
        String fileName9 = "file.TxT";

        // When & Then
        assertTrue(PATTERN.matcher(fileName1).matches(), "最短文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName2).matches(), "短文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName3).matches(), "纯数字文件名应该匹配");
        assertTrue(PATTERN.matcher(fileName4).matches(), "扩展名为数字应该匹配");
        assertTrue(PATTERN.matcher(fileName5).matches(), "数字文件名主体应该匹配");
        assertTrue(PATTERN.matcher(fileName6).matches(), "长文件名主体应该匹配");
        assertTrue(PATTERN.matcher(fileName7).matches(), "大写扩展名应该匹配");
        assertTrue(PATTERN.matcher(fileName8).matches(), "大写扩展名应该匹配");
        assertTrue(PATTERN.matcher(fileName9).matches(), "混合大小写扩展名应该匹配");
    }
}