package com.ricky.common.constants;

public interface LLMChatConstants {

    String GENERATE_SUMMARY_PROMPT = """
            请为以下文档生成中文摘要，要求：总结核心内容，语言精炼专业，字数不超过100字。输出仅包含摘要正文，不加任何解释和标签。
            """;

}
