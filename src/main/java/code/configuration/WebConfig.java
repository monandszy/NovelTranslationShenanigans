package code.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableWebMvc
@Slf4j
public class WebConfig implements WebMvcConfigurer {

  public static final int TIMEOUT = 120000;

  @Bean
  public WebClient webClient(final ObjectMapper objectMapper) {
    var httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
      .responseTimeout(Duration.ofMillis(TIMEOUT))
      .doOnConnected(conn ->
        conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
          .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));

    final int size = 16 * 1024 * 1024;
    var exchangeServices = ExchangeStrategies
      .builder()
      .codecs(c -> {
        c.defaultCodecs()
          .jackson2JsonEncoder(
            new Jackson2JsonEncoder(
              objectMapper,
              MediaType.APPLICATION_JSON
            ));
        c.defaultCodecs()
          .jackson2JsonDecoder(
            new Jackson2JsonDecoder(
              objectMapper,
              MediaType.APPLICATION_JSON
            ));
        c.defaultCodecs().maxInMemorySize(size);
      }).build();

    String API_URL = "https://generativelanguage.googleapis.com/";
    return WebClient.builder()
      .exchangeStrategies(exchangeServices)
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .baseUrl(API_URL)
      .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")
      .defaultHeader(HttpHeaders.ACCEPT, "application/json;charset=UTF-8")
      .filter((request, next) -> {
        log.info(">> {} {}", request.method(), request.url());
        log.debug(request.headers().toString());

        return next.exchange(request).flatMap(res -> {
          log.info("<< {} ", res.statusCode().value());
          if (res.statusCode().isError()) {
            res = res.mutate().rawStatusCode(299).build();
          }
          return Mono.just(res);
        });
      })
      .build();
  }

}