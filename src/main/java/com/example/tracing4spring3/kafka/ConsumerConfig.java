package com.example.tracing4spring3.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.MicrometerConsumerListener;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ConsumerConfig {

  @Bean
  public KafkaReceiver<String, String> kafkaReceiver(final ObservationRegistry observationRegistry) {
    Map<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.2.89:29091");
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "test-1");
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.LATEST.name().toLowerCase());
    consumerConfig.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

    ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(consumerConfig);

    KafkaReceiver<String, String> kafkaReceiver = KafkaReceiver
        .create(receiverOptions
            .withObservation(observationRegistry)
            .atmostOnceCommitAheadSize(20)
            .subscription(Pattern.compile("chchoi.test.*")));

    kafkaReceiver
        .receive()
        .flatMap(record -> {
          Observation receiverObservation =
              KafkaReceiverObservation.RECEIVER_OBSERVATION.start(null,
                  KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                  () ->
                      new KafkaRecordReceiverContext(
                          record, "user.receiver", receiverOptions.bootstrapServers()),
                  observationRegistry);

          return Mono.just(record)
              .doOnNext(r -> log.info("topic: {}, key: {}, value: {}", r.topic(), r.key(), r.value()))
              .doOnTerminate(receiverObservation::stop)
              .doOnError(receiverObservation::error)
              .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));})
        .subscribe();

    return kafkaReceiver;
  }
}

//@Component
//@RequiredArgsConstructor
//@Slf4j
//class ConsumerInitiator implements ApplicationListener<ApplicationReadyEvent> {
//  private final KafkaReceiver<String, String> kafkaReceiver;
//
//  @Override
//  public void onApplicationEvent(final ApplicationReadyEvent event) {
//    this.kafkaReceiver
//        .receive()
//        .doOnNext(stringStringConsumerRecord -> {
//          log.info("next....");
//        })
//        .onErrorContinue((e, o) -> log.error("error on consume", e))
//        .subscribe(record -> log.info("topic: {}, key: {}, value: {}", record.topic(), record.key(), record.value()));
//  }
//}