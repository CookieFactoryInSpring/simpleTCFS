package fr.univcotedazur.simpletcfs.cli.commands;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
class RestTestConfig {

    public static final String TEST_BASE_URL = "http://test";

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(TEST_BASE_URL) // dummy base URL
                .build();
    }
}
