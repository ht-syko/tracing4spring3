package com.example.tracing4spring3.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RestRouter {

  @Bean
  public RouterFunction<ServerResponse> restRoute(final RestToSMR restToSMR) { // to : execute.devices.{deviceId}.states
    return RouterFunctions.route()
        .GET("/test", request -> restToSMR.getToSMR(request.queryParam("to").orElseThrow()))
        .build();
  }
}
