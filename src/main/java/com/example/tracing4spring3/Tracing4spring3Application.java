package com.example.tracing4spring3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class Tracing4spring3Application {

  public static void main(String[] args) {
    hooks();
    SpringApplication.run(Tracing4spring3Application.class, args);
  }

  private static void hooks() {
    Hooks.enableAutomaticContextPropagation();
  }
}
