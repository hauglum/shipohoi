package no.hauglum.ship_o_hoi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.hauglum.ship_o_hoi.auth.BarentsWatchTokenService;
import no.hauglum.ship_o_hoi.model.AISShip;
import no.hauglum.ship_o_hoi.parser.GeoJsonAISParser;
import no.hauglum.ship_o_hoi.stream.LineAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;


import java.nio.charset.StandardCharsets;

@Service
public class BarentsWatchAISService {

    private final WebClient webClient;
    private final BarentsWatchTokenService tokenService;
    private final GeoJsonAISParser parser = new GeoJsonAISParser();

    public BarentsWatchAISService(
            @Qualifier("aisWebClient") WebClient webClient,
            BarentsWatchTokenService tokenService
    ) {
        this.webClient = webClient;
        this.tokenService = tokenService;
    }

    public Flux<AISShip> streamShips() {
        return tokenService.getAccessToken()
                .flatMapMany(token ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/v1/combined")
                                        .queryParam("modelType", "Full")
                                        .queryParam("modelFormat", "Geojson")
                                        .queryParam("downsample", true)
                                        .build()
                                )

                                .headers(h -> h.setBearerAuth(token))
                                .exchangeToFlux(response ->
                                        response.bodyToFlux(DataBuffer.class)
                                )
                )
                .transform(this::decodeLines)
                .map(parser::parseLine)
//                .doOnNext(ship -> System.out.println("🚢 " + ship))
                .doOnError(e -> System.err.println("❌ " + e));
    }

    private Flux<String> decodeLines(Flux<DataBuffer> buffers) {
        LineAccumulator accumulator = new LineAccumulator();

        return buffers.flatMap(buffer -> {
            String chunk = buffer.toString(StandardCharsets.UTF_8);
            DataBufferUtils.release(buffer);

            return Flux.fromIterable(accumulator.append(chunk))
                    .filter(line -> !line.isBlank());
        });
    }
}