package com.ricky.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ricky.common.utils.MyObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static org.springframework.boot.convert.DurationStyle.detectAndParse;

@Slf4j
@AutoConfiguration
public class ElasticSearchAutoConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Value("${spring.elasticsearch.username:#{null}}")
    private String username;

    @Value("${spring.elasticsearch.password:#{null}}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:30s}")
    private String socketTimeout;

    @Value("${spring.elasticsearch.max-connections:30}")
    private int maxConnections;

    @Value("${spring.elasticsearch.max-connections-per-route:10}")
    private int maxConnectionsPerRoute;

    @Value("${spring.elasticsearch.enable-ssl:false}")
    private boolean enableSsl;

    @Bean
    public ElasticsearchClient elasticsearchClient(MyObjectMapper objectMapper) {
        String[] uriArray = uris.split(",");
        HttpHost[] httpHosts = Arrays.stream(uriArray)
                .map(String::trim)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            // 配置认证
            configureAuthentication(httpClientBuilder);

            // 配置连接池
            httpClientBuilder.setMaxConnTotal(maxConnections);
            httpClientBuilder.setMaxConnPerRoute(maxConnectionsPerRoute);

            // 配置连接存活时间（使用 Duration 解析）
            Duration connectionTimeoutDuration = detectAndParse(connectionTimeout);
            httpClientBuilder.setConnectionTimeToLive(
                    connectionTimeoutDuration.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            // SSL 配置（如果需要）
            if (enableSsl || uris.trim().startsWith("https://")) {
                configureSsl(httpClientBuilder);
            }

            return httpClientBuilder;
        });

        // 配置请求超时
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
            Duration connectTimeout = detectAndParse(connectionTimeout);
            Duration socketTimeoutDuration = detectAndParse(socketTimeout);

            return requestConfigBuilder
                    .setConnectTimeout((int) connectTimeout.toMillis())
                    .setSocketTimeout((int) socketTimeoutDuration.toMillis())
                    .setConnectionRequestTimeout((int) connectTimeout.toMillis());
        });

        // 配置默认请求头
        restClientBuilder.setDefaultHeaders(configureDefaultHeaders());

        // 创建 RestClient
        RestClient restClient = restClientBuilder.build();

        // 创建 JacksonJsonpMapper
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

        // 创建 Transport
        RestClientTransport transport = new RestClientTransport(restClient, jsonpMapper);

        // 创建 ElasticsearchClient
        ElasticsearchClient client = new ElasticsearchClient(transport);

        log.info("ElasticsearchClient 初始化完成");
        return client;
    }

    @Bean
    public boolean checkElasticsearchConnection(ElasticsearchClient client) {
        try {
            boolean available = client.ping().value();
            log.info("Elasticsearch 连接检查: {}", available ? "成功" : "失败");
            return available;
        } catch (Exception e) {
            log.error("Elasticsearch 连接检查失败", e);
            return false;
        }
    }

    private void configureAuthentication(HttpAsyncClientBuilder httpClientBuilder) {
        if (isNotBlank(username) && isNotBlank(password)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
            );
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            log.debug("已配置 Elasticsearch 基本认证，用户: {}", username);
        } else {
            log.info("未配置 Elasticsearch 认证信息，将使用匿名连接");
        }
    }

    private void configureSsl(HttpAsyncClientBuilder httpClientBuilder) {
        try {
            // 开发环境：跳过 SSL 证书验证
            // 生产环境应该使用正式证书
            if (Boolean.parseBoolean(System.getProperty("dev.mode", "true"))) {
                // 创建信任所有证书的 SSLContext
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, new javax.net.ssl.TrustManager[]{
                        new javax.net.ssl.X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                }, new java.security.SecureRandom());

                httpClientBuilder.setSSLContext(sslContext);
                httpClientBuilder.setSSLHostnameVerifier(
                        (hostname, session) -> true // 跳过主机名验证
                );

                log.warn("已配置 Elasticsearch SSL 跳过验证（仅限开发环境使用！）");
            } else {
                log.info("生产环境 SSL 配置需要提供证书文件");
            }
        } catch (Exception e) {
            log.error("配置 Elasticsearch SSL 失败", e);
            throw new RuntimeException("SSL 配置失败", e);
        }
    }

    private BasicHeader[] configureDefaultHeaders() {
        return new BasicHeader[]{
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("User-Agent", "MPCS"),
                new BasicHeader("Connection", "keep-alive"), // 添加连接保持
        };
    }

}
