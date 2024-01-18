package com.example.tracing4spring3.rsocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RSocketService {
  private final RSocketRequester rSocketRequester;

  public Mono<?> rsocketFnF(final String routePath) {
    log.info("rsocket request start");
    return this.rSocketRequester
        .route(routePath)
        .data(Map.of("result", "success"))
        .send()
        .then(Mono.just(Map.of("result", "success")));
  }
}
