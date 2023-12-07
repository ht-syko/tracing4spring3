package com.example.tracing4spring3.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ProducerConfig {
  @Bean
  public KafkaSender<String, Object> kafkaSender(final ObservationRegistry observationRegistry) {
    Map<String, Object> senderProps = new HashMap<>();
    senderProps.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.89:29091");
    senderProps.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    senderProps.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    senderProps.put("spring.json.add.type.headers", false);

    SenderOptions<String, Object> senderOptions = SenderOptions.create(senderProps);
    return KafkaSender.create(senderOptions.withObservation(observationRegistry));
  }

  @Bean
  public RouterFunction<ServerResponse> produceRoute(final KafkaSender<String, Object> kafkaSender) {
    return RouterFunctions.route()
        .GET("/test/k/p", request -> kafkaSender
            .send(Flux
                .range(1, 2)
                .map(i -> {
                  ProducerRecord<String, Object> record =
                      new ProducerRecord<>(
                          "chchoi.test.2",
                          String.valueOf(i),
                          (Object) ("{\"id\":\"xxx\",\"message\":\"test\"}"));
                  log.info("produce data >> {}", record);
                  return SenderRecord.create(record, i);
                }))
            .doOnError(e -> log.error("send error", e))
            .doOnNext(response -> log.info("response: cm: {}, rm: {}", response.correlationMetadata(), response.recordMetadata()))
            .collectList()
            .flatMap(x -> ServerResponse.ok().build()))
        .build();
  }
}
