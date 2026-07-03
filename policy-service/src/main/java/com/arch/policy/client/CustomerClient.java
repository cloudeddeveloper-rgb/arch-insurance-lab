package com.arch.policy.client;

import com.arch.policy.client.dto.DownstreamDtos.CustomerView;
import com.arch.policy.config.ServiceProperties;
import com.arch.policy.exception.DownstreamException;
import com.arch.policy.exception.UnprocessableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class CustomerClient {

    private final RestClient restClient;

    // Boot auto-configures a prototype-scoped RestClient.Builder; each client
    // gets its own instance and pins its own base URL.
    public CustomerClient(RestClient.Builder builder, ServiceProperties props) {
        this.restClient = builder.baseUrl(props.customerBaseUrl()).build();
    }

    public CustomerView getCustomer(UUID customerId) {
        return restClient.get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new UnprocessableException("Customer does not exist: " + customerId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new DownstreamException("customer-service is unavailable");
                })
                .body(CustomerView.class);
    }
}
