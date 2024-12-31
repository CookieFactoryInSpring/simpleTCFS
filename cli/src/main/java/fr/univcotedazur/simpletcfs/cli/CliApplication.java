package fr.univcotedazur.simpletcfs.cli;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@SpringBootApplication
public class CliApplication {

    @Value("${tcf.host.baseurl}")
    private String serverHostandPort;

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(CliApplication.class, args), () -> 0);
    }

    // Shared WebClient for all commands
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Configuring the Connection Timeout and Response Timeout with a Netty HttpClient
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(3));
        return builder
                .baseUrl(serverHostandPort)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
