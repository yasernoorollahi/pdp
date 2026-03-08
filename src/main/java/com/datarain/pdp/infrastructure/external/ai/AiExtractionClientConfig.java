package com.datarain.pdp.infrastructure.external.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(AiExtractionProperties.class)
public class AiExtractionClientConfig {

    @Bean(name = "aiExtractionRestTemplate")
    public RestTemplate aiExtractionRestTemplate(RestTemplateBuilder builder,
                                                 AiExtractionProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }
}
