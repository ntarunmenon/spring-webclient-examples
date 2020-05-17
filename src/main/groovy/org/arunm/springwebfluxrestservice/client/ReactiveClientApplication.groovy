package org.arunm.springwebfluxrestservice.client

import groovy.json.JsonOutput
import org.arunm.springwebfluxrestservice.Event
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class ReactiveClientApplication {

    @Bean
    WebClient client() {
        return WebClient.create("http://localhost:8080")
    }

    @Bean
    CommandLineRunner getFluxDemoRetrieve (WebClient client) {
        return { args ->
            client.get().uri("/events")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(Event)
            .subscribe { println "getFluxDemo $it" }
        }
    }


    @Bean
    CommandLineRunner getFluxDemoExchange (WebClient client) {
        return { args ->
            client.get().uri("/events")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .flatMapMany { response -> response.bodyToFlux(Event.class) }
                    .subscribe { println "getFluxDemoExchange --> $it" }
        }
    }

    @Bean
    CommandLineRunner postDemo (WebClient client) {
        return { args ->
            client.post()
                    .uri("/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(JsonOutput.toJson(new Event(3L)))
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe { println "postDemo $it" }
        }
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveClientApplication)
            .properties(Collections.singletonMap("server.port","8081"))
            .run(args)
    }
}
