package com.example.tracing4spring3.rsocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RSocketService {
  private final RSocketRequester rSocketRequester;

  public Mono<?> rsocketFnF(final String routePath) {
    log.info("rsocket request start");

    Map<String, Object> data = new HashMap<>();
    data.put("parentDeviceId", "00d4f7a6084e4332aed828b8f1781ee6");
    Map<String, String> trigger = new HashMap<>();
    trigger.put("userId", "testUser");
    trigger.put("clientId", "ht-iot-app-ios-v1");
    trigger.put("deviceId", "DeviceId");
    trigger.put("type", "service");

    data.put("trigger", trigger);


    ArrayList<Map<String, String>> states = new ArrayList<>();
    Map<String, String> state1 = new HashMap<>();
    state1.put("name", "power");
    state1.put("value", "off");

    Map<String, String> state2 = new HashMap<>();
    state2.put("name", "setTemperature");
    state2.put("value", "25");
    states.add(state1);
    states.add(state2);

    data.put("states", states);

    return this.rSocketRequester
        .route(routePath)
        .data(data)
        .send()
        .then(Mono.just(Map.of("result", "success")));
  }

  public Mono<?>rsocketRnR(final String routePath) {
    return this.rSocketRequester
            .route(routePath)
            .retrieveMono(Object.class);
  }
}
