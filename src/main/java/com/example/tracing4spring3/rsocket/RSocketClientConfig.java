package com.example.tracing4spring3.rsocket;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;
import io.rsocket.micrometer.observation.ByteBufGetter;
import io.rsocket.micrometer.observation.ByteBufSetter;
import io.rsocket.micrometer.observation.ObservationRequesterRSocketProxy;
import io.rsocket.micrometer.observation.ObservationResponderRSocketProxy;
import io.rsocket.micrometer.observation.RSocketRequesterTracingObservationHandler;
import io.rsocket.micrometer.observation.RSocketResponderTracingObservationHandler;
import io.rsocket.plugins.InterceptorRegistry;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;


// https://github.com/micrometer-metrics/micrometer-samples/blob/bb777d40daacd0dc108e20731ce4dc4f72d47a2f/rsocket-client/src/main/java/com/example/micrometer/ManualConfiguration.java
@Configuration
public class RSocketClientConfig {
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  RSocketResponderTracingObservationHandler rSocketResponderTracingObservationHandler(Tracer tracer,
                                                                                      Propagator propagator) {
    return new RSocketResponderTracingObservationHandler(tracer, propagator, new ByteBufGetter(), true);
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  RSocketRequesterTracingObservationHandler rSocketRequesterTracingObservationHandler(Tracer tracer,
                                                                                      Propagator propagator) {
    return new RSocketRequesterTracingObservationHandler(tracer, propagator, new ByteBufSetter(), true);
  }

  @Bean
  ObservationRSocketConnectorConfigurer observationRSocketConnectorConfigurer(
      ObservationRegistry observationRegistry) {
    return new ObservationRSocketConnectorConfigurer(observationRegistry);
  }

  @Bean
  ObservationRSocketServerCustomizer observationRSocketServerCustomizer(ObservationRegistry observationRegistry) {
    return new ObservationRSocketServerCustomizer(observationRegistry);
  }

  @Bean
  public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
    return builder
        .rsocketStrategies(strategiesBuilder -> strategiesBuilder
            .encoder(new Jackson2JsonEncoder())
            .decoder(new Jackson2JsonDecoder()))
        .dataMimeType(MimeType.valueOf("application/json"))
        .transport(TcpClientTransport.create("192.168.2.82", 9084));
  }
}

class ObservationRSocketConnectorConfigurer implements RSocketConnectorConfigurer {

  private final ObservationRegistry observationRegistry;

  ObservationRSocketConnectorConfigurer(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
  }

  @Override
  public void configure(RSocketConnector rSocketConnector) {
    rSocketConnector.interceptors(ir -> ir.forResponder(
            (RSocketInterceptor) rSocket -> new ObservationResponderRSocketProxy(rSocket, this.observationRegistry))
        .forRequester((RSocketInterceptor) rSocket -> new ObservationRequesterRSocketProxy(rSocket,
            this.observationRegistry)));
  }

}

class ObservationRSocketServerCustomizer implements RSocketServerCustomizer {

  private final ObservationRegistry observationRegistry;

  ObservationRSocketServerCustomizer(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
  }

  @Override
  public void customize(RSocketServer rSocketServer) {
    rSocketServer.interceptors(ir -> ir.forResponder(
            (RSocketInterceptor) rSocket -> new ObservationResponderRSocketProxy(rSocket, this.observationRegistry))
        .forRequester((RSocketInterceptor) rSocket -> new ObservationRequesterRSocketProxy(rSocket,
            this.observationRegistry)));
  }

}