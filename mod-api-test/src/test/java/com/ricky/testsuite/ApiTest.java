package com.ricky.testsuite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 轻量化 Api 测试工具：支持 multipart、JSON、表单、Query、Headers、Token、泛型反序列化等
 */
@Slf4j
public class ApiTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private String url;
    private HttpMethod method = HttpMethod.GET;

    private final MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
    private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final List<MultipartFile> files = new ArrayList<>();

    private Object body = null;
    private boolean logEnabled = true;

    public ApiTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
    }

    /* ------------ HTTP 方法 ------------ */
    public ApiTest get(String url) {
        this.method = HttpMethod.GET;
        this.url = url;
        return this;
    }

    public ApiTest post(String url) {
        this.method = HttpMethod.POST;
        this.url = url;
        return this;
    }

    public ApiTest put(String url) {
        this.method = HttpMethod.PUT;
        this.url = url;
        return this;
    }

    public ApiTest patch(String url) {
        this.method = HttpMethod.PATCH;
        this.url = url;
        return this;
    }

    public ApiTest delete(String url) {
        this.method = HttpMethod.DELETE;
        this.url = url;
        return this;
    }

    /* ------------ 请求配置 ------------ */
    public ApiTest param(String k, String v) {
        formParams.add(k, v == null ? "" : v);
        return this;
    }

    public ApiTest query(String k, String v) {
        queryParams.add(k, v == null ? "" : v);
        return this;
    }

    public ApiTest body(Object body) {
        this.body = body;
        return this;
    }

    public ApiTest file(MultipartFile file) {
        if (file != null) files.add(file);
        return this;
    }

    public ApiTest header(String k, String v) {
        headers.put(k, v);
        return this;
    }

    public ApiTest headers(Map<String, String> map) {
        headers.putAll(map);
        return this;
    }

    public ApiTest bearerToken(String token) {
        headers.put("Authorization", "Bearer " + token);
        return this;
    }

    public ApiTest disableLog() {
        this.logEnabled = false;
        return this;
    }

    /**
     * @brief 执行请求
     */
    public ResponseExecutor execute() {
        try {
            RequestBuilder builder = buildRequest();
            logRequest();
            MvcResult result = mockMvc.perform(builder).andReturn();
            logResponse(result);
            return new ResponseExecutor(result);
        } catch (Exception e) {
            throw new ApiTestException("API 请求失败", e);
        }
    }

    /**
     * @brief 请求构建
     */
    private RequestBuilder buildRequest() {
        String fullUrl = buildUrlWithQuery();

        // Multipart (POST/PUT)
        if (!files.isEmpty() && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            var multipartBuilder = MockMvcRequestBuilders.multipart(fullUrl);

            multipartBuilder.with(req -> {
                req.setMethod(method.name());
                return req;
            });

            files.forEach(f -> multipartBuilder.file(toMockMultipart(f)));
            formParams.forEach((k, v) -> v.forEach(vv -> multipartBuilder.param(k, vv)));
            headers.forEach(multipartBuilder::header);

            return multipartBuilder;
        }

        // JSON body
        if (body != null) {
            var rb = MockMvcRequestBuilders.request(method, fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(body));
            headers.forEach(rb::header);
            return rb;
        }

        // Form 或无 body
        var rb = MockMvcRequestBuilders.request(method, fullUrl);
        if (!formParams.isEmpty()) {
            rb.contentType(MediaType.APPLICATION_FORM_URLENCODED);
            rb.params(formParams);
        }
        headers.forEach(rb::header);

        return rb;
    }

    private String buildUrlWithQuery() {
        if (queryParams.isEmpty()) return url;

        StringBuilder sb = new StringBuilder(url).append("?");
        queryParams.forEach((k, vs) -> vs.forEach(v -> sb.append(k).append("=").append(encode(v)).append("&")));
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String encode(String v) {
        if (v == null) return "";
        return v.replace(" ", "%20").replace("&", "%26");
    }

    private MockMultipartFile toMockMultipart(MultipartFile f) {
        try {
            return f instanceof MockMultipartFile ? (MockMultipartFile) f :
                    new MockMultipartFile(f.getName(), f.getOriginalFilename(), f.getContentType(), f.getInputStream());
        } catch (IOException e) {
            throw new ApiTestException("MultipartFile 转换失败", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ApiTestException("JSON 序列化失败", e);
        }
    }

    /**
     * @brief 日志
     */
    private void logRequest() {
        if (!logEnabled) return;
        log.info(">>> {} {}", method, url);
    }

    private void logResponse(MvcResult result) {
        if (!logEnabled) return;
        log.info("<<< Status: {}", result.getResponse().getStatus());
    }

    /**
     * @brief 响应处理
     */
    public class ResponseExecutor {
        private final MvcResult result;

        ResponseExecutor(MvcResult result) {
            this.result = result;
        }

        public ResponseExecutor expectStatus(int status) {
            int actual = result.getResponse().getStatus();
            if (actual != status) {
                throw new ApiTestException("状态码不符合：期望 " + status + " 实际 " + actual);
            }
            return this;
        }

        public ResponseExecutor expectUserMessage(String expectMessage) {
            JsonNode jsonBody = parseJson();
            assertEquals(expectMessage, jsonBody.get("userMessage").asText(), "业务消息不符预期");
            return this;
        }

        public String asString() {
            try {
                return result.getResponse().getContentAsString();
            } catch (Exception e) {
                throw new ApiTestException("读取响应失败", e);
            }
        }

        public <T> T as(Class<T> type) {
            try {
                String c = result.getResponse().getContentAsString();
                if (type == String.class) return type.cast(c);
                if (type == Void.class) return null;
                if (type == Integer.class) return type.cast(Integer.valueOf(c));
                if (type == Object.class) return type.cast(objectMapper.readTree(c));
                return objectMapper.readValue(c, type);
            } catch (Exception e) {
                throw new ApiTestException("反序列化失败", e);
            }
        }

        public <T> T as(TypeReference<T> typeRef) {
            try {
                return objectMapper.readValue(result.getResponse().getContentAsString(), typeRef);
            } catch (Exception e) {
                throw new ApiTestException("泛型反序列化失败", e);
            }
        }

        private JsonNode parseJson() {
            try {
                String content = result.getResponse().getContentAsString(UTF_8);
                return content.isEmpty() ?
                        objectMapper.createObjectNode() :
                        objectMapper.readTree(content);
            } catch (UnsupportedEncodingException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @brief 接口测试异常
     */
    public static class ApiTestException extends RuntimeException {
        public ApiTestException(String msg) {
            super(msg);
        }

        public ApiTestException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
