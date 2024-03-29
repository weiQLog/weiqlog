package com.weiqlog.weiqlog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataSourcePropConfig.class)
public class WeiqlogApplication {

    private final Map<String, SseEmitter> sses = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(WeiqlogApplication.class, args);
    }

//    @Bean
//    public IntegrationFlow inboundFlow(@Value("${input-dir:file://C:\\Users\\weiql\\Desktop\\in}") File in) {
//        System.out.println(in.getAbsolutePath());
//        return IntegrationFlows.from(Files.inboundAdapter(in).autoCreateDirectory(true), poller -> poller.poller(spec -> spec.fixedRate(1000L)))
//                .transform(File.class, File::getAbsolutePath).handle(String.class, (path, map) -> {
//                    sses.forEach((key, sse) -> {
//                                try {
//                                    sse.send(path);
//                                } catch (Exception ex) {
//                                    throw new RuntimeException();
//                                }
//                            }
//
//                    );
//                    return null;
//                })
//                .channel(filesChannel())
//                .get();
//    }
//
//    @Bean
//    SubscribableChannel filesChannel() {
//        return MessageChannels.publishSubscribe().get();
//    }
//
//    @GetMapping("/files/{name}")
//    SseEmitter file(@PathVariable String name) {
//        SseEmitter sseEmitter = new SseEmitter(60 * 1000L);
//        sses.put(name, sseEmitter);
//        return sseEmitter;
//
//    }

    @GetMapping(value="files/{name}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> files(@PathVariable String name) {
        return Flux.create((FluxSink<String> sink) -> {
//            FluxSink<String> serialize = sink.serialize();
            MessageHandler handler = msg -> sink.next(String.class.cast(msg.getPayload()));
//            serialize.setCancellation(() -> filesChannel().unsubscribe(handler));
            filesChannel().subscribe(handler);
        });
    }

    @Bean
    public IntegrationFlow inboundFlow(@Value("${input-dir:file://C:\\Users\\weiql\\Desktop\\in}") File in) {
        System.out.println(in.getAbsolutePath());
        return IntegrationFlow.from(Files.inboundAdapter(in).autoCreateDirectory(true), poller -> poller.poller(spec -> spec.fixedRate(1000L)))
                .transform(File.class, File::getAbsolutePath)
                .channel(filesChannel())
                .get();
    }

    @Bean
    SubscribableChannel filesChannel() {
        return MessageChannels.publishSubscribe().get();
    }

}
