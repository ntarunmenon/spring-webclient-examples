package org.arunm.springwebfluxrestservice.client

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.logging.LoggingHandler
import org.arunm.springwebfluxrestservice.Event
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.channel.BootstrapHandlers
import reactor.netty.http.client.HttpClient

import java.nio.charset.Charset

import static io.netty.util.internal.PlatformDependent.allocateUninitializedArray
import static java.lang.Math.max
import static java.nio.charset.Charset.defaultCharset


@SpringBootApplication
class ReactiveClientApplication {

    @Bean
    WebClient client() {
        return WebClient
            .builder()
            .baseUrl("http://localhost:8080")
            .clientConnector(new ReactorClientHttpConnector(httpClient()))
            .build()
    }

    private HttpClient httpClient() {
        return HttpClient.create()
        .tcpConfiguration { tc ->
            tc.bootstrap { b ->
                BootstrapHandlers.updateLogSupport(b, new CustomLogger(HttpClient))
            }
        }
    }

    private class CustomLogger extends LoggingHandler {
        CustomLogger(Class<?> clazz) {
            super(clazz);
        }

        @Override
        protected String format(ChannelHandlerContext ctx, String event, Object arg) {
            if (arg instanceof ByteBuf) {
                ByteBuf msg = (ByteBuf) arg;
                return decode(msg, msg.readerIndex(), msg.readableBytes(), defaultCharset());
            }
            return super.format(ctx, event, arg);
        }

        private String decode(ByteBuf src, int readerIndex, int len, Charset charset) {
            if (len != 0) {
                byte[] array;
                int offset;
                if (src.hasArray()) {
                    array = src.array();
                    offset = src.arrayOffset() + readerIndex;
                } else {
                    array = allocateUninitializedArray(max(len, 1024));
                    offset = 0;
                    src.getBytes(readerIndex, array, 0, len);
                }
                return new String(array, offset, len, charset);
            }
            return "";
        }
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
                    .toEntity(Event)
                    .subscribe { println "postDemo $it" }
        }
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveClientApplication)
            .properties(Collections.singletonMap("server.port","8081"))
            .run(args)
    }
}
