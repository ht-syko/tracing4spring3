package com.example.tracing4spring3.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestToSMR {
  private final WebClient webClient;

  public Mono<ServerResponse> getToSMR(final String path) {
    log.info("getToSMR start");
    return this.webClient.get()
        .uri("http://127.0.0.1:9083" + path)
        .retrieve()
        .bodyToMono(HashMap.class)
        .doOnNext(res -> log.info("res: {}", res))
        .flatMap(res -> ServerResponse.ok().bodyValue(res));
  }
}
