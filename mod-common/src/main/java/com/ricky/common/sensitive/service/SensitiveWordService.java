package com.ricky.common.sensitive.service;

import java.io.IOException;
import java.io.InputStream;

public interface SensitiveWordService {

    /**
     * 判断文本中是否含有敏感词
     *
     * @param text 文本
     * @return true=有 false=没有
     */
    boolean hasSensitiveWord(String text);

    /**
     * 从输入流读取内容并判断是否含有敏感词
     *
     * @param stream 输入流
     * @return true=包含敏感词，false=不包含敏感词或流为空
     * @throws IOException 如果读取输入流时发生I/O错误
     */
    boolean hasSensitiveWord(InputStream stream) throws IOException;

    /**
     * 敏感词替换<br>
     * 过滤文本中的敏感词
     *
     * @param text 待替换文本
     * @return 替换后的文本
     */
    String filter(String text);

    /**
     * 从输入流读取内容并过滤敏感词
     *
     * @param stream 输入流
     * @return 过滤后的文本内容（敏感词被替换为特定字符），如果流为空则返回空字符串
     * @throws IOException 如果读取输入流时发生I/O错误
     */
    String filter(InputStream stream) throws IOException;

}
