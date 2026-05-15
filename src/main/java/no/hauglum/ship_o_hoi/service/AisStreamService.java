package no.hauglum.ship_o_hoi.service;

import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.parser.AisStreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Service
public class AisStreamService {

    private static final Logger log = LoggerFactory.getLogger(AisStreamService.class);
    private static final String WS_URL = "wss://stream.aisstream.io/v0/stream";

    private final String apiKey;
    private final AisStreamParser parser = new AisStreamParser();

    public AisStreamService(@Value("${aisstream.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public Flux<AISShip> streamShips() {
        String subscribeMsg = """
                {"APIKey":"%s","BoundingBoxes":[[[-90,-180],[90,180]]],"FilterMessageTypes":["ShipStaticData"]}
                """.formatted(apiKey).strip();

        return Flux.create(sink -> {
            ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

            Disposable disposable = client.execute(URI.create(WS_URL), session ->
                    session.send(Mono.just(session.textMessage(subscribeMsg)))
                            .thenMany(session.receive()
                                    .map(WebSocketMessage::getPayloadAsText)
                                    .map(parser::parseLine)
                                    .filter(Objects::nonNull)
                                    .doOnNext(sink::next))
                            .then()
            ).subscribe(
                    null,
                    sink::error,
                    () -> sink.error(new RuntimeException("AISStream WebSocket session closed"))
            );

            sink.onCancel(disposable::dispose);
            sink.onDispose(disposable::dispose);
        });
    }
}
