package com.arch.policy.client;

import com.arch.policy.client.dto.DownstreamDtos.QuoteView;
import com.arch.policy.config.ServiceProperties;
import com.arch.policy.exception.DownstreamException;
import com.arch.policy.exception.UnprocessableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class RatingClient {

    private final RestClient restClient;

    public RatingClient(RestClient.Builder builder, ServiceProperties props) {
        this.restClient = builder.baseUrl(props.ratingBaseUrl()).build();
    }

    public QuoteView getQuote(UUID quoteId) {
        return restClient.get()
                .uri("/api/v1/quotes/{id}", quoteId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new UnprocessableException("Quote does not exist: " + quoteId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new DownstreamException("rating-service is unavailable");
                })
                .body(QuoteView.class);
    }
}
