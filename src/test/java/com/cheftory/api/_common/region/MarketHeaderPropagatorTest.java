package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@DisplayName("Market WebClient Header Propagator Tests")
class MarketHeaderPropagatorTest {

    private final ExchangeFilterFunction propagator = (request, next) -> {
        var info = MarketContext.currentOrNull();
        if (info == null) return next.exchange(request);

        ClientRequest filtered = ClientRequest.from(request)
                .header(MarketHeaders.COUNTRY_CODE, info.countryCode())
                .build();

        return next.exchange(filtered);
    };

    @Test
    void shouldAddHeader_whenContextPresent() {
        ExchangeFunction exchange = mock(ExchangeFunction.class);
        ClientResponse mockResponse = mock(ClientResponse.class);

        when(exchange.exchange(any(ClientRequest.class))).thenReturn(Mono.just(mockResponse));

        ClientRequest req = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/api"))
                .build();

        try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
            ClientResponse res = propagator.filter(req, exchange).block();
            assertThat(res).isEqualTo(mockResponse);

            verify(exchange).exchange(org.mockito.ArgumentMatchers.argThat(r -> "KR"
                    .equals(r.headers().getFirst(MarketHeaders.COUNTRY_CODE))));
        }
    }

    @Test
    void shouldNotTouchRequest_whenNoContext() {
        ExchangeFunction exchange = mock(ExchangeFunction.class);
        ClientResponse mockResponse = mock(ClientResponse.class);

        when(exchange.exchange(any(ClientRequest.class))).thenReturn(Mono.just(mockResponse));

        ClientRequest req = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/api"))
                .build();

        ClientResponse res = propagator.filter(req, exchange).block();
        assertThat(res).isEqualTo(mockResponse);

        verify(exchange)
                .exchange(org.mockito.ArgumentMatchers.argThat(
                        r -> r.headers().getFirst(MarketHeaders.COUNTRY_CODE) == null));
    }
}
