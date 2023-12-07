package com.example.tracing4spring3.rsocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RSocketRestRouter {
  @Bean
  public RouterFunction<?> rsocketRoute(RSocketService rSocketService) {
    return RouterFunctions.route()
        .GET("/test/r", request -> rSocketService.rsocketFnF(request.queryParam("to").orElseThrow()).flatMap(x -> ServerResponse.ok().bodyValue(x)))
        .build();
  }
}
