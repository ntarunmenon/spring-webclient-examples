package org.arunm.springwebfluxrestservice.webservice

import org.arunm.springwebfluxrestservice.Event
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.time.Duration
import java.time.LocalDate
import java.util.stream.Stream

@SpringBootApplication
@RestController
class ReactiveServiceApplication {

    @GetMapping("/events/{id}")
    Mono<Event> eventById(@PathVariable long id){
         Mono.just(new Event(id:1, date:LocalDate.now()));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/events")
    Flux<Event> events() {
        Flux<Event> eventFlux = Flux.fromStream(Stream.generate {new Event(System.currentTimeMillis(), LocalDate.now())})
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(1))
        return Flux.zip(eventFlux, durationFlux)
        .map {it.t1}
    }

    @PostMapping("/event")
    @ResponseStatus(HttpStatus.CREATED)
    void add(@RequestBody Event event) {
        println "got event: $event"
    }

    static void main(String[] args) {
        SpringApplication.run(ReactiveServiceApplication)
    }
}
