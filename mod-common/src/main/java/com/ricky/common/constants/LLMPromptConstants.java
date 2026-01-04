package com.ricky.common.constants;

public interface LLMPromptConstants {

    String FILE_SUMMARY_PROMPT = """
            你是一个专业的文档摘要助手。请基于用户提供的文本，生成一个简洁、准确、连贯的摘要。要求：
            1. 捕捉核心事实、主要观点和关键结论
            2. 保持客观中立，不添加个人观点
            3. 确保摘要自成段落，逻辑流畅
            4. 避免直接复制原文句子，要进行概括
            """;

}
