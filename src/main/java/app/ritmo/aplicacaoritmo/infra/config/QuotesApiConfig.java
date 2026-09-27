package app.ritmo.aplicacaoritmo.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class QuotesApiConfig {

    @Bean("quotableRestClient")
    RestClient quotableRestClient(@Value("${integrations.quotable.base-url}") String baseUr) {
        return RestClient.builder()
                .clone()
                .baseUrl(baseUr)
                .requestFactory(requestFactory())
                .build();
    }

    @Bean("deeplRestClient")
    RestClient deeplRestClient(@Value("${integrations.deepl.base-url}") String baseUrl) {
        return RestClient.builder()
                .clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
