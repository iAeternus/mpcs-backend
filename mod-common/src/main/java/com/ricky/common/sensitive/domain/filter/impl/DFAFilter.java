package com.ricky.common.sensitive.domain.filter.impl;

import com.ricky.common.constants.ConfigConstants;
import com.ricky.common.sensitive.domain.filter.SensitiveWordFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/8/21
 * @className DFAFilter
 * @desc DFA敏感词算法
 */
public final class DFAFilter implements SensitiveWordFilter {

    /**
     * 敏感词字典的根节点
     */
    private static Word root = new Word();

    /**
     * 替代字符
     */
    private final static char MASK_CHAR = '*';

    /**
     * 遇到这些字符就会跳过
     */
    private final static String skipChars = " !*-+_=,，.@;:；：。、？?（）()【】[]《》<>“”\"‘’";

    /**
     * 遇到这些字符就会跳过
     */
    private final static Set<Character> skipSet = new HashSet<>();

    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    private DFAFilter() {
    }

    public static DFAFilter getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean hasSensitiveWord(String text) {
        if (isBlank(text)) {
            return false;
        }
        return !Objects.equals(filter(text), text);
    }

    @Override
    public String filter(String text) {
        StringBuilder result = new StringBuilder(text);
        int index = 0;
        while (index < result.length()) {
            char c = result.charAt(index);
            if (skip(c)) {
                index++;
                continue;
            }
            Word word = root;
            int start = index;
            boolean found = false;
            for (int i = index; i < result.length(); i++) {
                c = result.charAt(i);
                if (skip(c)) {
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    c += 32;
                }
                word = word.next.get(c);
                if (word == null) {
                    break;
                }
                if (word.end) {
                    found = true;
                    for (int j = start; j <= i; j++) {
                        result.setCharAt(j, MASK_CHAR);
                    }
                    index = i;
                }
            }
            if (!found) {
                index++;
            }
        }
        return result.toString();
    }

    @Override
    public void loadWord(List<String> words) {
        if (!isEmpty(words)) {
            Word newRoot = new Word();
            words.forEach(word -> loadWord(word, newRoot));
            root = newRoot;
        }
    }

    /**
     * 加载敏感词
     *
     * @param word 词
     */
    private void loadWord(String word, Word root) {
        if (isBlank(word)) {
            return;
        }
        Word current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            // 如果是大写字母, 转换为小写
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            if (skip(c)) {
                continue;
            }
            current = current.next.computeIfAbsent(c, k -> new Word());
        }
        current.end = true;
    }


    /**
     * 从文本文件中加载敏感词列表
     *
     * @param path 文本文件的绝对路径
     */
    public void loadWordFromFile(String path) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            loadWord(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从流中加载敏感词列表
     *
     * @param inputStream 文本文件输入流
     * @throws IOException IO异常
     */
    private void loadWord(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CHARSET))) {
            String line;
            ArrayList<String> list = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            loadWord(list);
        }
    }

    /**
     * 判断是否需要跳过当前字符
     *
     * @param c 待检测字符
     * @return true: 需要跳过, false: 不需要跳过
     */
    private boolean skip(char c) {
        return skipSet.contains(c);
    }

    /**
     * 敏感词类
     */
    private static class Word {

        // 结束标识
        private boolean end;

        // 下一层级的敏感词字典
        private final Map<Character, Word> next;

        public Word() {
            // 当前字符
            this.next = new HashMap<>();
        }
    }

    private static class Holder {
        private static final DFAFilter INSTANCE = new DFAFilter();
    }
}
